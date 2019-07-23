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

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Runtime Kinesis consumer processor.
 */
public class KinesisShardProcessorFactory implements ShardRecordProcessorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisShardProcessor.class);

    private final String kinesisStreamName;
    private final ObjectMapper objectMapper;
    private final KinesisTransport transport;
    private final Consumer<byte[]> handleMessageCallback;
    private final KinesisPayloadDecoder kinesisPayloadDecoder;

    public KinesisShardProcessorFactory(
            AWSMessageType awsMessageType,
            ObjectMapper objectMapper,
            KinesisTransport transport,
            String kinesisStreamName,
            Consumer<byte[]> handleMessageCallback) {
        this.kinesisStreamName = kinesisStreamName;
        this.transport = transport;
        this.handleMessageCallback = requireNonNull(handleMessageCallback, "dataHandler");
        this.objectMapper = objectMapper;
        this.kinesisPayloadDecoder = new KinesisPayloadDecoder(objectMapper, awsMessageType, kinesisStreamName);
    }

    @Override
    public ShardRecordProcessor shardRecordProcessor() {
        return new KinesisShardProcessor();
    }

    public class KinesisShardProcessor implements ShardRecordProcessor {

        private DateTime lastCheckpoint = DateTime.now();
        private CountDownLatch testThrottleLatch;


        @Override
        public void initialize(InitializationInput initializationInput) {
            LOG.debug("Initializing Kinesis worker for stream <{}>", kinesisStreamName);
        }

        @Override
        public void processRecords(ProcessRecordsInput processRecordsInput) {

            LOG.info("processRecords called. Received {} Kinesis events", processRecordsInput.records().size());

            // Configurable throttle for testing purposes.
            // TODO: Remove
            try {
                int min = 1;
                int max = 30;
                Random r = new Random();
                int randomInt = r.nextInt((max - min) + 1) + min;
                LOG.info("Before throttle for [{}] seconds", randomInt);
                testThrottleLatch = new CountDownLatch(1);
                testThrottleLatch.await(randomInt, TimeUnit.SECONDS);
                LOG.info("After throttle for [{}] seconds", randomInt);
            } catch (InterruptedException e) {
                LOG.error("Test throttling exception",e);
            }

            if (transport.isThrottled()) {
                LOG.info("[throttled] The Kinesis consumer will pause message processing until the throttle state clears,");
                transport.blockUntilUnthrottled();
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
                        handleMessageCallback.accept(objectMapper.writeValueAsBytes(kinesisLogEntry));
                    }

                } catch (Exception e) {
                    LOG.error("Couldn't read Kinesis record from stream <{}>", kinesisStreamName, e);
                }
            }

            // Periodically checkpoint the stream.
            if (lastCheckpoint.plusMinutes(1).isBeforeNow()) {
                lastCheckpoint = DateTime.now();
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
            LOG.debug("Checkpointing stream <{}>", kinesisStreamName);
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
}
