package org.graylog.integrations.aws.transports;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.cloudwatch.CloudWatchLogSubscriptionData;
import org.graylog.integrations.aws.cloudwatch.KinesisLogEntry;
import org.graylog2.plugin.Tools;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for decoding the raw Kinesis byte array payload.
 */
public class KinesisPayloadDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisPayloadDecoder.class);

    private final ObjectMapper objectMapper;
    private final AWSMessageType awsMessageType;
    private final String kinesisStream;

    @Inject
    public KinesisPayloadDecoder(ObjectMapper objectMapper, AWSMessageType awsMessageType, String kinesisStream) {
        this.objectMapper = objectMapper;
        this.awsMessageType = awsMessageType;
        this.kinesisStream = kinesisStream;
    }

    /**
     * Decodes the raw Kinesis byte array message payload.
     *
     * <p>The following {@link AWSMessageType} enum values are supported:</p>
     *
     * <p>
     * <strong>{@code AWSMessageType.KINESIS_RAW}</strong>: Raw Kinesis log messages that are converted directly to a string.
     * <strong>{@code AWSMessageType.KINESIS_FLOW_LOGS}</strong>: CloudWatch flow log messages. These messages are
     * delivered to Kinesis from CloudWatch in batches within a JSON document.
     * </p>
     *
     * @param payloadBytes A Kinesis payload in byte array form.
     * @param approximateArrivalTimestamp The approximate instant that the message was written to Kinesis. This is used
     *                                    for the {@code AWSMessageType.KINESIS_RAW} message timestamp.
     * @return A list of {@link KinesisLogEntry} messages, which are fully ready to be written to the Graylog Journal.
     * @throws IOException
     */
    List<KinesisLogEntry> processMessages(final byte[] payloadBytes, Instant approximateArrivalTimestamp) throws IOException {

        // Rely on the AWSMessageType identified in the setup healthCheck.
        if (awsMessageType == AWSMessageType.KINESIS_FLOW_LOGS) {
            CloudWatchLogSubscriptionData logSubscriptionData = decompressCloudWatchMessages(payloadBytes, kinesisStream);

            // convert to list of raw messages
            return logSubscriptionData.logEvents().stream()
                                      .map(le -> {
                                          DateTime timestamp = new DateTime(le.timestamp(), DateTimeZone.UTC);
                                          return KinesisLogEntry.create(kinesisStream,
                                                                        // Use the log group and stream returned from CloudWatch.
                                                                        logSubscriptionData.logGroup(),
                                                                        logSubscriptionData.logStream(), timestamp, le.message());
                                      })
                                      .collect(Collectors.toList());
        } else if (awsMessageType == AWSMessageType.KINESIS_RAW) {
            // The best timestamp available is the approximate arrival time of the message to the Kinesis stream.
            DateTime timestamp = new DateTime(approximateArrivalTimestamp.toEpochMilli(), DateTimeZone.UTC);

            // Parse the Flow Log message
            KinesisLogEntry kinesisLogEntry = KinesisLogEntry.create(kinesisStream,
                                                                     null, null,
                                                                     timestamp, new String(payloadBytes));
            return Collections.singletonList(kinesisLogEntry);
        } else {
            LOG.error("The AWSMessageType [{}] is not supported by the KinesisTransport", awsMessageType);
            return new ArrayList<>();
        }
    }

    private CloudWatchLogSubscriptionData decompressCloudWatchMessages(byte[] payloadBytes, String streamName) throws IOException {

        LOG.debug("The supplied payload is GZip compressed. Proceeding to decompress and parse as a CloudWatch log message.");

        final byte[] bytes = Tools.decompressGzip(payloadBytes).getBytes();
        LOG.debug("They payload was decompressed successfully. size [{}]", bytes.length);

        // Assume that the payload is from CloudWatch.
        // Extract messages, so that they can be committed to journal one by one.
        final CloudWatchLogSubscriptionData logSubscriptionData = objectMapper.readValue(bytes, CloudWatchLogSubscriptionData.class);

        if (LOG.isTraceEnabled()) {
            // Log the number of events retrieved from CloudWatch. DO NOT log the content of the messages.
            LOG.trace("[{}] messages obtained from CloudWatch", logSubscriptionData.logEvents().size());
        }

        return logSubscriptionData;
    }
}
