package org.graylog.integrations.aws.transports;


import org.graylog.integrations.aws.service.AWSService;
import org.graylog2.plugin.system.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClientBuilder;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;

public class KinesisConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisConsumer.class);
    private static final int GRACEFUL_SHUTDOWN_TIMEOUT = 20;
    private static final TimeUnit GRACEFUL_SHUTDOWN_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final Region region;
    private final String kinesisStreamName;
    private final NodeId nodeId;
    private final Integer maxThrottledWaitMillis;
    private final Integer recordBatchSize;

    private final StaticCredentialsProvider credentialsProvider;

    private Scheduler scheduler;
    private KinesisTransport transport;
    private ShardRecordProcessorFactory recordProcessorFactory;

    KinesisConsumer(String kinesisStream,
                    Region region,
                    String awsKey, String awsSecret, NodeId nodeId,
                    KinesisTransport transport,
                    Integer maxThrottledWaitMillis,
                    Integer recordBatchSize) {
        this.kinesisStreamName = requireNonNull(kinesisStream, "kinesisStream");
        this.region = requireNonNull(region, "region");
        this.nodeId = requireNonNull(nodeId, "nodeId");
        this.transport = transport;
        this.maxThrottledWaitMillis = maxThrottledWaitMillis;
        this.recordBatchSize = recordBatchSize;
        this.credentialsProvider = AWSService.buildCredentialProvider(awsKey, awsSecret);
    }

    // TODO metrics
    public void run() {

        transport.consumerState.set(KinesisTransportState.STARTING);

        LOG.debug("Max wait millis [{}]", maxThrottledWaitMillis);
        LOG.debug("Record batch size [{}]", recordBatchSize);

        // Default max records is 10k. This can be overridden from UI.
        // if (recordBatchSize != null) {
        //    config.withMaxRecords(recordBatchSize);
        //}

        // TODO: add Optional HTTP proxy
        //if (awsConfig.proxyEnabled() && proxyUrl != null) {
        //    config.withCommonClientConfig(Proxy.forAWS(proxyUrl));
        //}

        // Create the clients needed for the Kinesis consumer.
        final DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder()
                                                                    .region(region)
                                                                    .credentialsProvider(credentialsProvider)
                                                                    .build();
        final CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder()
                                                                            .region(region)
                                                                            .credentialsProvider(credentialsProvider)
                                                                            .build();
        final KinesisAsyncClientBuilder kinesisAsyncClientBuilder = KinesisAsyncClient.builder()
                                                                                      .region(this.region)
                                                                                      .credentialsProvider(credentialsProvider);
        final KinesisAsyncClient kinesisAsyncClient = KinesisClientUtil.createKinesisAsyncClient(kinesisAsyncClientBuilder);

        final String workerId = String.format(Locale.ENGLISH, "graylog-node-%s", nodeId.anonymize());

        // The application name needs to be unique per input. Using the same name for two different Kinesis
        // streams will cause trouble with state handling in DynamoDB. (used by the Kinesis client under the
        // hood to keep state)
        final String applicationName = String.format(Locale.ENGLISH, "graylog-aws-plugin-%s", kinesisStreamName);

        ConfigsBuilder configsBuilder = new ConfigsBuilder(kinesisStreamName, applicationName,
                                                           kinesisAsyncClient, dynamoClient, cloudWatchClient,
                                                           workerId,
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