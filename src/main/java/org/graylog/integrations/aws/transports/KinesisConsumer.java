package org.graylog.integrations.aws.transports;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import okhttp3.HttpUrl;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.cloudwatch.KinesisLogEntry;
import org.graylog2.plugin.system.NodeId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.exceptions.ThrottlingException;
import software.amazon.kinesis.lifecycle.events.InitializationInput;
import software.amazon.kinesis.lifecycle.events.LeaseLostInput;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.lifecycle.events.ShardEndedInput;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;
import software.amazon.kinesis.retrieval.KinesisClientRecord;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class KinesisConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisConsumer.class);
    private static final int GRACEFUL_SHUTDOWN_TIMEOUT = 20;
    private static final TimeUnit GRACEFUL_SHUTDOWN_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final Region region;
    private final String kinesisStreamName;
    private final NodeId nodeId;
    private final HttpUrl proxyUrl;
    private final AWSPluginConfiguration awsConfig;
    private final Consumer<byte[]> dataHandler;
    private final Integer maxThrottledWaitMillis;
    private final Integer recordBatchSize;
    private final KinesisPayloadDecoder kinesisPayloadDecoder;
    private final KinesisAsyncClient kinesisAsyncClient;

    private Scheduler scheduler;
    private KinesisTransport transport;
    private final ObjectMapper objectMapper;
    /**
     * Checkpointing must be performed when the KinesisConsumer needs to be shuts down due to sustained throttling.
     * At the time when shutdown occurs, checkpointing might not have happened for a while, so we keep track of the
     * last sequence to checkpoint to.
     */
    private String lastSuccessfulRecordSequence = null;

    public KinesisConsumer(String kinesisStream,
                           Region region,
                           Consumer<byte[]> dataHandler,
                           AWSPluginConfiguration awsConfig,
                           String awsKey, String awsSecret, NodeId nodeId,
                           @Nullable HttpUrl proxyUrl,
                           KinesisTransport transport,
                           ObjectMapper objectMapper,
                           Integer maxThrottledWaitMillis,
                           Integer recordBatchSize,
                           AWSMessageType awsMessageType) {
        this.kinesisStreamName = requireNonNull(kinesisStream, "kinesisStream");
        this.region = requireNonNull(region, "region");
        this.dataHandler = requireNonNull(dataHandler, "dataHandler");
        this.awsConfig = requireNonNull(awsConfig, "awsConfig");
        this.authProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsKey, awsSecret));
        this.nodeId = requireNonNull(nodeId, "nodeId");
        this.proxyUrl = proxyUrl;
        this.transport = transport;
        this.objectMapper = objectMapper;
        this.maxThrottledWaitMillis = maxThrottledWaitMillis;
        this.recordBatchSize = recordBatchSize;
        this.kinesisPayloadDecoder = new KinesisPayloadDecoder(objectMapper, awsMessageType, kinesisStream);
        this.kinesisAsyncClient = KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder().region(this.region));
    }

    // TODO metrics
    public void run() {

        transport.consumerState.set(KinesisTransportState.STARTING);

        LOG.debug("Max wait millis [{}]", maxThrottledWaitMillis);
        LOG.debug("Record batch size [{}]", recordBatchSize);

        final String workerId = String.format(Locale.ENGLISH, "graylog-node-%s", nodeId.anonymize());

        // The application name needs to be unique per input. Using the same name for two different Kinesis
        // streams will cause trouble with state handling in DynamoDB. (used by the Kinesis client under the
        // hood to keep state)
        final String applicationName = String.format(Locale.ENGLISH, "graylog-aws-plugin-%s", kinesisStreamName);
        KinesisClientLibConfiguration config = new KinesisClientLibConfiguration(applicationName, kinesisStreamName,
                                                                                 authProvider, workerId);

        // Default max records is 10k. This can be overridden from UI.
        if (recordBatchSize != null) {
            config.withMaxRecords(recordBatchSize);
        }

        // Optional HTTP proxy
        if (awsConfig.proxyEnabled() && proxyUrl != null) {
            config.withCommonClientConfig(Proxy.forAWS(proxyUrl));
        }

        final ShardRecordProcessorFactory recordProcessorFactory = () -> new ShardRecordProcessor() {
            private DateTime lastCheckpoint = DateTime.now();

            @Override
            public void initialize(InitializationInput initializationInput) {
                LOG.debug("Initializing Kinesis worker for stream <{}>", kinesisStreamName);
                transport.consumerState.set(KinesisTransportState.RUNNING);
            }

            @Override
            public void processRecords(ProcessRecordsInput processRecordsInput) {

                LOG.debug("processRecords called. Received {} Kinesis events", processRecordsInput.records().size());

                if (transport.isThrottled()) {
                    LOG.info("[throttled] Waiting up to [{}ms] for throttling to clear.", maxThrottledWaitMillis);
                    if (!transport.blockUntilUnthrottled(maxThrottledWaitMillis, TimeUnit.MILLISECONDS)) {

                        /* Stop the Kinesis consumer when throttling does not clear quickly. The AWS Kinesis client
                         * requires that the worker thread stays healthy and does not take too long to respond.
                         * So, if we need to wait a long time for throttling to clear (eg. more than 1 minute), then the
                         * consumer needs to be shutdown and restarted later once throttling clears. */
                        LOG.info("[throttled] Throttling did not clear in [{}]ms. Stopping the Kinesis worker to let " +
                                 "the throttle clear.️ It will start again automatically once throttling clears.", maxThrottledWaitMillis);

                        // Checkpoint last processed record before shutting down.
                        if (lastSuccessfulRecordSequence != null) {
                            checkpoint(processRecordsInput, lastSuccessfulRecordSequence);
                        }

                        transport.consumerState.set(KinesisTransportState.STOPPING);
                        scheduler.shutdown();
                        transport.stoppedDueToThrottling.set(true);
                        return;
                    }

                    LOG.debug("[unthrottled] Kinesis consumer will now resume processing records.");
                }

                for (KinesisClientRecord record : processRecordsInput.records()) {
                    LOG.info("Processing Kinesis records.");
                    try {
                        // Create a read-only view of the data and use a safe method to convert it to a byte array
                        // as documented in Record#getData(). (using ByteBuffer#array() can fail)
                        final ByteBuffer dataBuffer = record.data().asReadOnlyBuffer();
                        final byte[] dataBytes = new byte[dataBuffer.remaining()];
                        dataBuffer.get(dataBytes);

                        List<KinesisLogEntry> kinesisLogEntries =
                                kinesisPayloadDecoder.processMessages(dataBytes,
                                                                      record.approximateArrivalTimestamp());

                        for (KinesisLogEntry kinesisLogEntry : kinesisLogEntries) {
                            dataHandler.accept(objectMapper.writeValueAsBytes(kinesisLogEntry));
                        }

                        lastSuccessfulRecordSequence = record.sequenceNumber();
                    } catch (Exception e) {
                        LOG.error("Couldn't read Kinesis record from stream <{}>", kinesisStreamName, e);
                    }
                }

                // According to the Kinesis client documentation, we should not checkpoint for every record but
                // rather periodically.
                // TODO: Make interval configurable (global)
                if (lastCheckpoint.plusMinutes(1).isBeforeNow()) {
                    lastCheckpoint = DateTime.now();
                    LOG.debug("Checkpointing stream <{}>", kinesisStreamName);
                    checkpoint(processRecordsInput, null);
                }
            }

            @Override
            public void leaseLost(LeaseLostInput leaseLostInput) {

            }

            @Override
            public void shardEnded(ShardEndedInput shardEndedInput) {

            }

            @Override
            public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
                LOG.info("Shutting down Kinesis worker for stream <{}>", kinesisStreamName);
            }

            private void checkpoint(ProcessRecordsInput processRecordsInput, String lastSequence) {
                final Retryer<Void> retryer = RetryerBuilder.<Void>newBuilder()
                        .retryIfExceptionOfType(ThrottlingException.class)
                        .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                        .withStopStrategy(StopStrategies.stopAfterDelay(10, TimeUnit.MINUTES))
                        .withRetryListener(new RetryListener() {
                            @Override
                            public <V> void onRetry(Attempt<V> attempt) {
                                if (attempt.hasException()) {
                                    LOG.warn("Checkpointing stream <{}> failed, retrying. (attempt {})", kinesisStreamName, attempt.getAttemptNumber());
                                }
                            }
                        })
                        .build();

                try {
                    retryer.call(() -> {
                        try {
                            if (lastSequence != null) {
                                processRecordsInput.checkpointer().checkpoint(lastSequence);
                            } else {
                                processRecordsInput.checkpointer().checkpoint();
                            }
                        } catch (InvalidStateException e) {
                            LOG.error("Couldn't save checkpoint to DynamoDB table used by the Kinesis client library - check database table", e);
                        } catch (ShutdownException e) {
                            LOG.debug("Processor is shutting down, skipping checkpoint");
                        }
                        return null;
                    });
                } catch (ExecutionException e) {
                    LOG.error("Couldn't checkpoint stream <{}>", kinesisStreamName, e);
                } catch (RetryException e) {
                    LOG.error("Checkpoint retry for stream <{}> finally failed", kinesisStreamName, e);
                }
            }
        };

        DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder().region(region).build();
        CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder().region(region).build();
        ConfigsBuilder configsBuilder = new ConfigsBuilder(kinesisStreamName, kinesisStreamName,
                                                           kinesisAsyncClient, dynamoClient, cloudWatchClient,
                                                           UUID.randomUUID().toString(),
                                                           recordProcessorFactory);


        /*
         * The Scheduler (also called Worker in earlier versions of the KCL) is the entry point to the KCL. This
         * instance is configured with defaults provided by the ConfigsBuilder.
         */
        this.scheduler = new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(),
                configsBuilder.processorConfig(),
                configsBuilder.retrievalConfig().retrievalSpecificConfig(new PollingConfig(kinesisStreamName,
                                                                                           kinesisAsyncClient)));

        LOG.debug("Starting Kinesis scheduler.");
        scheduler.run();
        transport.consumerState.set(KinesisTransportState.STOPPED);
        LOG.debug("After Kinesis scheduler stopped.");
    }

    /**
     * Stops the KinesisConsumer. Finishes processing the current batch of data already received from Kinesis
     * before shutting down.
     */
    public void stop() {
        if (scheduler != null) {
            Future<Boolean> gracefulShutdownFuture = scheduler.startGracefulShutdown();
            LOG.info("Waiting up to 20 seconds for shutdown to complete.");
            try {
                gracefulShutdownFuture.get(GRACEFUL_SHUTDOWN_TIMEOUT, GRACEFUL_SHUTDOWN_TIMEOUT_UNIT);
            } catch (InterruptedException e) {
                LOG.info("Interrupted while waiting for graceful shutdown. Continuing.");
            } catch (ExecutionException e) {
                LOG.error("Exception while executing graceful shutdown.", e);
            } catch (TimeoutException e) {
                LOG.error("Timeout while waiting for shutdown.  Scheduler may not have exited.");
            }
            LOG.info("Shutting down consumer");
        }
    }
}