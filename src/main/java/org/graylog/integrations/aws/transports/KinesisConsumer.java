package org.graylog.integrations.aws.transports;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.graylog.integrations.aws.AWSMessageType;
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
import software.amazon.kinesis.retrieval.polling.PollingConfig;

import java.util.Locale;
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

    private final String kinesisStreamName;
    private final Region region;
    private final NodeId nodeId;
    private final KinesisTransport transport;
    private final Integer maxThrottledWaitMillis;
    private final Integer recordBatchSize;
    private final ObjectMapper objectMapper;
    private final AWSMessageType awsMessageType;
    private final StaticCredentialsProvider credentialsProvider;
    private final Consumer<byte[]> handleMessageCallback;
    private Scheduler kinesisScheduler;

    KinesisConsumer(String kinesisStreamName,
                    Region region,
                    String awsKey,
                    String awsSecret,
                    NodeId nodeId,
                    KinesisTransport transport,
                    Integer maxThrottledWaitMillis,
                    Integer recordBatchSize,
                    ObjectMapper objectMapper,
                    AWSMessageType awsMessageType, Consumer<byte[]> handleMessageCallback) {

        Preconditions.checkArgument(StringUtils.isNotBlank(kinesisStreamName), "A Kinesis stream name is required.");
        Preconditions.checkNotNull(region, "A Region is required.");
        Preconditions.checkNotNull(awsMessageType, "A AWSMessageType is required.");

        this.kinesisStreamName = requireNonNull(kinesisStreamName, "kinesisStream");
        this.region = requireNonNull(region, "region");
        this.nodeId = requireNonNull(nodeId, "nodeId");
        this.transport = transport;
        this.maxThrottledWaitMillis = maxThrottledWaitMillis;
        this.recordBatchSize = recordBatchSize;
        this.objectMapper = objectMapper;
        this.awsMessageType = awsMessageType;
        this.credentialsProvider = AWSService.buildCredentialProvider(awsKey, awsSecret);
        this.handleMessageCallback = handleMessageCallback;
    }

    // TODO metrics
    public void run() {

        LOG.debug("Max wait millis [{}]", maxThrottledWaitMillis);
        LOG.debug("Record batch size [{}]", recordBatchSize);

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

        final KinesisShardProcessorFactory kinesisShardProcessorFactory = new KinesisShardProcessorFactory(awsMessageType,
                                                                                                           objectMapper,
                                                                                                           transport,
                                                                                                           kinesisStreamName,
                                                                                                           maxThrottledWaitMillis,
                                                                                                           handleMessageCallback);


        ConfigsBuilder configsBuilder = new ConfigsBuilder(kinesisStreamName, applicationName,
                                                           kinesisAsyncClient, dynamoClient, cloudWatchClient,
                                                           workerId,
                                                           kinesisShardProcessorFactory);

        /*
         * The Scheduler (also called Worker in earlier versions of the KCL) is the entry point to the KCL. This
         * instance is configured with defaults provided by the ConfigsBuilder.
         */
        final PollingConfig pollingConfig = new PollingConfig(kinesisStreamName, kinesisAsyncClient);

        // Default max records is 10k. This can be overridden from UI.
        if (recordBatchSize != null) {
            pollingConfig.maxRecords(recordBatchSize);
            pollingConfig.idleTimeBetweenReadsInMillis(recordBatchSize);
        }
        this.kinesisScheduler = new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(),
                configsBuilder.processorConfig(),
                configsBuilder.retrievalConfig().retrievalSpecificConfig(pollingConfig));

        LOG.debug("Starting Kinesis scheduler.");
        kinesisScheduler.run();
        LOG.debug("After Kinesis scheduler stopped.");
    }

    /**
     * Stops the KinesisConsumer. Finishes processing the current batch of data already received from Kinesis
     * before shutting down.
     */
    public void stop() {
        if (kinesisScheduler != null) {
            Future<Boolean> gracefulShutdownFuture = kinesisScheduler.startGracefulShutdown();
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