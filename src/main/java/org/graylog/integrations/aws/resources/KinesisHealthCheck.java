package org.graylog.integrations.aws.resources;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * This class attempts to connect to the indicated Kinesis stream and read one log message. If the stream exists
 * and a message can be read, then the message will be returned.
 *
 * This class does not do parsing of any kind. The binary payload from the first Kinesis record will be returned.
 * That payload might be GZIPed (if from a CloudWatch subscription), or just a plain binary string if sent from
 * somewhere else. The responsibility of parsing and unpacking the GZIPPED payload falls on the caller of this class.
 *
 * A full Kinesis subscription is executed, which uses DynamoDB for state tracking. This effectively tests most of the
 * permissions needed in order to read log messages from the Kinesis stream later at runtime.
 *
 * TODO: Figure out the correct way to allow callers to wait for a response for a specified timeout.
 * If the timeout is exceeded, then then the client instance will need to be shut down gracefully.
 */
public class KinesisHealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisHealthCheck.class);

    private final String streamName;
    private final Region region;
    private final KinesisAsyncClient kinesisClient;
    private final String applicationName;

    private KinesisHealthCheck(String streamName, String region) {

        // Validate input.
        Preconditions.checkArgument(StringUtils.isNotBlank(streamName), "Stream Name must not be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(region), "Region must not be blank");

        this.streamName = streamName;
        this.region = Region.of(region);
        this.kinesisClient = KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder().region(this.region));
        this.applicationName = createRandomApplicationName();
    }

    public void start() {

        run();
    }

    private void run() {

        ScheduledExecutorService producerExecutor = Executors.newSingleThreadScheduledExecutor();

        // The application name is a unique identifier that is used when creating the Kinesis Subscriber.
        final String applicationName = createRandomApplicationName();

        final DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder().region(region).build();
        final CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder().region(region).build();
        final ConfigsBuilder configsBuilder = new ConfigsBuilder(streamName, applicationName, kinesisClient, dynamoClient, cloudWatchClient, UUID.randomUUID().toString(), new SampleRecordProcessorFactory());

        final RetrievalConfig retrievalConfig = configsBuilder.retrievalConfig();

        // TODO: TRIM_HORIZON reads from the beginning of the stream. Ideally this would read from the end of the stream. Check if this is possible.
        retrievalConfig.initialPositionInStreamExtended(InitialPositionInStreamExtended.newInitialPosition(InitialPositionInStream.TRIM_HORIZON));

        // Only pull 1 record.
        final PollingConfig pollingConfig = new PollingConfig(streamName, kinesisClient);
        pollingConfig.maxRecords(1);

        final Scheduler scheduler = new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(),
                configsBuilder.processorConfig(),
                retrievalConfig.retrievalSpecificConfig(pollingConfig)
        );

        final Thread schedulerThread = new Thread(scheduler);
        schedulerThread.setDaemon(true);
        schedulerThread.start();

        System.out.println("Press enter to shutdown");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (IOException ioex) {
            LOG.error("Caught exception while waiting for confirm. Shutting down.", ioex);
        }

        producerExecutor.shutdownNow();

        final Future<Boolean> gracefulShutdownFuture = scheduler.startGracefulShutdown();
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

    // TODO: Is static keyword needed here?
    private class SampleRecordProcessorFactory implements ShardRecordProcessorFactory {
        public ShardRecordProcessor shardRecordProcessor() {
            return new KinesisProcessor();
        }
    }

    /**
     * The application name is a unique identifier that is used when creating the Kinesis Subscriber. State tracking
     * tables/rows in DynamoDB will automatically be created using this application name.
     *
     * @return a random string application name starting with the string {@code "KinesisHealthCheck"}, so it can be
     * easily identified.
     */
    private String createRandomApplicationName() {

        // Prefix the application
        String applicationName = String.format("KinesisHealthCheck-%s-%s", streamName, UUID.randomUUID().toString());
        LOG.debug("Using application name [{}]", this.applicationName);
        return applicationName;
    }

    // TODO: Is static keyword needed here?
    private class KinesisProcessor implements ShardRecordProcessor {

        private final Logger log = LoggerFactory.getLogger(KinesisProcessor.class);

        // Called at startup time.
        public void initialize(InitializationInput initializationInput) {
            log.debug("Initializing healthCheck for application name [{}]", KinesisHealthCheck.this.applicationName);
        }

        /**
         * Called automatically by the AWS Kinesis Client library each time records are available for reading in
         * Kinesis.
         *
         * @param processRecordsInput provided by the AWS SDK and contains the retrieved records.
         */
        public void processRecords(ProcessRecordsInput processRecordsInput) {

            try {
                log.debug("Processing {} record(s)", processRecordsInput.records().size());
                Consumer<KinesisClientRecord> method = r -> {

                    log.trace("Processing record pk: {} -- Seq: {}", r.partitionKey(), r.sequenceNumber());

                    final ByteBuffer dataBuffer = processRecordsInput.records().get(0).data().asReadOnlyBuffer();
                    final byte[] dataBytes = new byte[dataBuffer.remaining()];
                    dataBuffer.get(dataBytes);

                    // TODO: Supply the receive data bytes to a callback method that the caller can subscribe to.
                };

                log.info("[{}] records received", processRecordsInput.records().size());
                processRecordsInput.records().forEach(method);
            } catch (Throwable t) {
                log.error("Caught throwable while processing records. Aborting.");
                Runtime.getRuntime().halt(1);
            }
        }

        // Handle case when Kinesis subscription lease is lost.
        public void leaseLost(LeaseLostInput leaseLostInput) {
            log.info("Lost lease, so terminating.");
        }

        // Not sure when this is called?
        public void shardEnded(ShardEndedInput shardEndedInput) {

            try {
                log.info("Reached shard end checkpointing.");
                shardEndedInput.checkpointer().checkpoint();
            } catch (Exception e) {
                log.error("Exception while checkpointing at shard end. Giving up.", e);
            }
        }

        /**
         * Since the Kinesis consumer maintains its own threads, we need to gracefully shut those down.
         * This is called when a shutdown is requested.
         */
        public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
            try {
                log.info("Scheduler is shutting down, checkpointing.");
                shutdownRequestedInput.checkpointer().checkpoint();
            } catch (Exception e) {
                log.error("Exception while checkpointing at requested shutdown. Giving up.", e);
            }
        }
    }

    /**
     * A tester that can be used to write messages into a Kinesis stream for testing purposes.
     * Keeping this inline for convenience. TODO: Consider removing in the future.
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
            LOG.info("Publishing a record");
            PutRecordRequest request = PutRecordRequest.builder()
                                                       .partitionKey(RandomStringUtils.randomAlphabetic(5, 20))
                                                       .streamName(streamName)
                                                       .data(SdkBytes.fromUtf8String("Test record"))
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

    /**
     * Use for testing only.
     * TODO: Remove this before release.
     *
     * @param args supply the stream and region in a space-separated format eg. test-stream us-east-1
     */
    public static void main(String... args) {

        if (args.length < 2) {
            LOG.error("The stream name and region are required as arguments.");
            System.exit(1);
        }

        String streamName = args[0];
        String region = args[1];

        new KinesisHealthCheck(streamName, region).run();
    }
}
