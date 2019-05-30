package org.graylog.integrations.aws.resources;

import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.InitialPositionInStream;
import software.amazon.kinesis.common.InitialPositionInStreamExtended;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.lifecycle.events.InitializationInput;
import software.amazon.kinesis.lifecycle.events.LeaseLostInput;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.lifecycle.events.ShardEndedInput;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;
import software.amazon.kinesis.retrieval.KinesisClientRecord;
import software.amazon.kinesis.retrieval.RetrievalConfig;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This class attempts to connect to the indicated Kinesis stream and read one log message.
 *
 * The connection is completed with a full Kinesis subscription which uses DynamoDB for state tracking
 * and all of the other parts that.
 */
public class KinesisHealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisHealthCheck.class);
    public static final boolean DO_PUBLISH = false;

    public static void main(String... args) {
        if (args.length < 1) {
            LOG.error("At a minimum, the stream name is required as the first argument. The Region may be specified as the second argument.");
            System.exit(1);
        }

        String streamName = args[0];
        String region = null;
        if (args.length > 1) {
            region = args[1];
        }

        new KinesisHealthCheck(streamName, region).run();
    }

    private final String streamName;
    private final Region region;
    private final KinesisAsyncClient kinesisClient;

    private KinesisHealthCheck(String streamName, String region) {
        this.streamName = streamName;
        this.region = Region.of(ObjectUtils.firstNonNull(region, "us-east-2"));
        this.kinesisClient = KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder().region(this.region));
    }

    private void run() {

        ScheduledExecutorService producerExecutor = Executors.newSingleThreadScheduledExecutor();

        DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder().region(region).build();
        CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder().region(region).build();

        ConfigsBuilder configsBuilder = new ConfigsBuilder(streamName, "Test", kinesisClient, dynamoClient, cloudWatchClient, UUID.randomUUID().toString(), new SampleRecordProcessorFactory());

        RetrievalConfig retrievalConfig = configsBuilder.retrievalConfig();
        retrievalConfig.initialPositionInStreamExtended(InitialPositionInStreamExtended.newInitialPosition(InitialPositionInStream.TRIM_HORIZON));

        // Only pull 1 record.
        PollingConfig pollingConfig = new PollingConfig(streamName, kinesisClient);
        pollingConfig.maxRecords(1);

        Scheduler scheduler = new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(),
                configsBuilder.processorConfig(),
                retrievalConfig.retrievalSpecificConfig(pollingConfig)
        );

        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.setDaemon(true);
        schedulerThread.start();

        System.out.println("Press enter to shutdown");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (IOException ioex) {
            LOG.error("Caught exception while waiting for confirm. Shutting down.", ioex);
        }

        producerExecutor.shutdownNow();

        Future<Boolean> gracefulShutdownFuture = scheduler.startGracefulShutdown();
        LOG.info("Waiting up to 20 seconds for shutdown to complete.");
        try {
            gracefulShutdownFuture.get(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.info("Interrupted while waiting for graceful shutdown. Continuing.");
        } catch (ExecutionException e) {
            LOG.error("Exception while executing graceful shutdown.", e);
        } catch (TimeoutException e) {
            LOG.error("Timeout while waiting for shutdown.  Scheduler may not have exited.");
        }
        LOG.info("Completed, shutting down now.");
    }

    private static class SampleRecordProcessorFactory implements ShardRecordProcessorFactory {
        public ShardRecordProcessor shardRecordProcessor() {
            return new KinesisProcessor();
        }
    }

    private static class KinesisProcessor implements ShardRecordProcessor {

        private static final String SHARD_ID_MDC_KEY = "ShardId";

        private static final Logger log = LoggerFactory.getLogger(KinesisProcessor.class);

        private String shardId;

        public final AtomicInteger receivedMessageCount = new AtomicInteger(0);

        // Called at startup time.
        public void initialize(InitializationInput initializationInput) {

            // TODO: Start up a scheduled thread that periodically writes logs into a CloudWatch group.
            shardId = initializationInput.shardId();
            MDC.put(SHARD_ID_MDC_KEY, shardId);
            try {
                log.info("Initializing @ Sequence: {}", initializationInput.extendedSequenceNumber());
            } finally {
                MDC.remove(SHARD_ID_MDC_KEY);
            }
        }

        // Called back each time records are available for processing.
        public void processRecords(ProcessRecordsInput processRecordsInput) {

            // TODO: Print received logs to the console.
            MDC.put(SHARD_ID_MDC_KEY, shardId);
            try {
                log.info("Processing {} record(s)", processRecordsInput.records().size());

                Consumer<KinesisClientRecord> method = r -> {
                    log.info("Processing record pk: {} -- Seq: {}", r.partitionKey(), r.sequenceNumber());


                    final ByteBuffer dataBuffer = processRecordsInput.records().get(0).data().asReadOnlyBuffer();
                    final byte[] dataBytes = new byte[dataBuffer.remaining()];
                    dataBuffer.get(dataBytes);

                    // TODO: Identify if payload is GZipped. If so, unpack.
                    // After unpacking, check if message is a CloudWatch logs container JSON. This will need to be
                    // parsed and the log messages extracted.

                    final ByteArrayInputStream dataStream = new ByteArrayInputStream(dataBytes);
                    try {

                        log.info("Received message # {}: {}", receivedMessageCount.incrementAndGet(),
                                 new String(ByteStreams.toByteArray(dataStream), StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                log.info("[{}] records received", processRecordsInput.records().size());
                processRecordsInput.records().forEach(method);

                // TODO: Bail after pulling one record.
            } catch (Throwable t) {
                log.error("Caught throwable while processing records. Aborting.");
                Runtime.getRuntime().halt(1);
            } finally {
                MDC.remove(SHARD_ID_MDC_KEY);
            }
        }

        // Handle case when Kinesis subscription lease is lost.
        public void leaseLost(LeaseLostInput leaseLostInput) {
            MDC.put(SHARD_ID_MDC_KEY, shardId);
            try {
                log.info("Lost lease, so terminating.");
            } finally {
                MDC.remove(SHARD_ID_MDC_KEY);
            }
        }

        // Not sure when this is called?
        public void shardEnded(ShardEndedInput shardEndedInput) {
            MDC.put(SHARD_ID_MDC_KEY, shardId);
            try {
                log.info("Reached shard end checkpointing.");
                shardEndedInput.checkpointer().checkpoint();
            } catch (Exception e) {
                log.error("Exception while checkpointing at shard end. Giving up.", e);
            } finally {
                MDC.remove(SHARD_ID_MDC_KEY);
            }
        }

        // Since the Kinesis consumer maintains its own threads, we need to gracefully shut those down.
        // This is called when a shutdown is requested.
        public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
            MDC.put(SHARD_ID_MDC_KEY, shardId);
            try {
                log.info("Scheduler is shutting down, checkpointing.");
                shutdownRequestedInput.checkpointer().checkpoint();
            } catch (Exception e) {
                log.error("Exception while checkpointing at requested shutdown. Giving up.", e);
            } finally {
                MDC.remove(SHARD_ID_MDC_KEY);
            }
        }
    }

    /**
     * A tester that can be used to write messages into a Kinesis stream.
     *
     * This will be needed when testing.
     */
    public class ProducerTester {

        ScheduledExecutorService producerExecutor;
        ScheduledFuture<?> producerFuture;

        /**
         * Start writing test records to Kinesis every 1 second.
         */
        public void start() {
            producerExecutor = Executors.newSingleThreadScheduledExecutor();
            producerFuture = producerExecutor.scheduleAtFixedRate(this::publishRecord, 10, 1, TimeUnit.SECONDS);
        }

        /**
         * Shut down the tester.
         */
        public void stop() {
            LOG.info("Cancelling producer and shutting down executor.");
            producerFuture.cancel(true);
        }

        // We won't need to publish data, because CloudWatch is going to do that for us.
        // This is only here for testing purposes.
        private void publishRecord() {
            if (DO_PUBLISH) {
                LOG.info("Publishing a record");
                PutRecordRequest request = PutRecordRequest.builder()
                                                           .partitionKey(RandomStringUtils.randomAlphabetic(5, 20))
                                                           .streamName(streamName)
                                                           .data(SdkBytes.fromUtf8String("hahaha"))
                                                           .build();
                try {
                    kinesisClient.putRecord(request).get();
                } catch (InterruptedException e) {
                    LOG.info("Interrupted, assuming shutdown.");
                } catch (ExecutionException e) {
                    LOG.error("Exception while sending data to Kinesis. Will try again next cycle.", e);
                }
            }
        }
    }
}
