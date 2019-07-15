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

public class KinesisTransportProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisTransportProcessor.class);

    private final ObjectMapper objectMapper;
    private final AWSMessageType awsMessageType;
    private final String kinesisStream;

    @Inject
    public KinesisTransportProcessor(ObjectMapper objectMapper, AWSMessageType awsMessageType, String kinesisStream) {
        this.objectMapper = objectMapper;
        this.awsMessageType = awsMessageType;
        this.kinesisStream = kinesisStream;
    }

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
