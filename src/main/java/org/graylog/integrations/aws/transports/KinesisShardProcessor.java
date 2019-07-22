package org.graylog.integrations.aws.transports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.cloudwatch.KinesisLogEntry;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class KinesisShardProcessor implements ShardRecordProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisShardProcessor.class);
    private final ObjectMapper objectMapper;
    private final AWSPluginConfiguration awsConfig;
    private final AWSMessageType awsMessageType;
    private final KinesisTransport transport;
    private final String kinesisStreamName;
    private final Integer maxThrottledWaitMillis;
    private final Consumer<byte[]> dataHandler;
    private final KinesisPayloadDecoder kinesisPayloadDecoder;

    public KinesisShardProcessor(
            AWSPluginConfiguration awsConfig,
            AWSMessageType awsMessageType,
            ObjectMapper objectMapper,
            KinesisTransport transport,
            String kinesisStreamName,
            Integer maxThrottledWaitMillis,
            Consumer<byte[]> dataHandler) {
        this.awsMessageType = awsMessageType;
        this.transport = transport;
        this.kinesisStreamName = kinesisStreamName;
        this.maxThrottledWaitMillis = maxThrottledWaitMillis;
        this.dataHandler = requireNonNull(dataHandler, "dataHandler");
        this.objectMapper = objectMapper;
        this.kinesisPayloadDecoder = new KinesisPayloadDecoder(objectMapper, awsMessageType, kinesisStreamName);
        this.awsConfig = requireNonNull(awsConfig, "awsConfig");
    }

    /**
     * Checkpointing must be performed when the KinesisConsumer needs to be shuts down due to sustained throttling.
     * At the time when shutdown occurs, checkpointing might not have happened for a while, so we keep track of the
     * last sequence to checkpoint to.
     */
    private String lastSuccessfulRecordSequence = null;

    // TODO: Should this really be declared to now?
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
                // TODO: Need to correctly shut down the thread scheduler.
                // I don't think leaking a reference is appropriate. Perhaps calling a method on the consumer/transport
                // is better?
                // scheduler.shutdown();
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
}
