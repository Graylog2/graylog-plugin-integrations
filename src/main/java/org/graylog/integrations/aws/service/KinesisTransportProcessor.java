package org.graylog.integrations.aws.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.cloudwatch.CloudWatchLogEvent;
import org.graylog.integrations.aws.cloudwatch.CloudWatchLogSubscriptionData;
import org.graylog.integrations.aws.cloudwatch.KinesisLogEntry;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.journal.RawMessage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.Record;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class KinesisTransportProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisTransportProcessor.class);

    private static final int EIGHT_BITS = 8;

    private final ObjectMapper objectMapper;
    private final String logGroup;
    private final String logStream;
    private final String kinesisStream;

    @Inject
    public KinesisTransportProcessor(KinesisClientBuilder kinesisClientBuilder,
                                     ObjectMapper objectMapper, String logGroup, String logStream, String kinesisStream) {

        this.logGroup = logGroup;
        this.logStream = logStream;
        this.kinesisStream = kinesisStream;
        this.objectMapper = objectMapper;
    }

    List<RawMessage> processMessages(Record record, AWSMessageType awsMessageType, String kinesisStreamName) throws IOException {

        final byte[] payloadBytes = record.data().asByteArray();
        if (isCompressed(payloadBytes)) {
            List<CloudWatchLogEvent> cloudWatchLogEvents = handleCompressedMessages(payloadBytes, kinesisStreamName);

            // convert to list of raw messages
            return cloudWatchLogEvents.stream()
                                      .map(le -> {
                                          DateTime timestamp = new DateTime(le.timestamp(), DateTimeZone.UTC);
                                          return KinesisLogEntry.create(kinesisStreamName, logGroup, logStream, timestamp, le.message());
                                      })
                                      .map(toRawMessage())
                                      .filter(Objects::nonNull)
                                      .collect(Collectors.toList());
        }

        // If message is not compressed, then just package it up as a single log message.

        // The best timestamp available is the approximate arrival time of the message to the Kinesis stream.
        DateTime timestamp = new DateTime(record.approximateArrivalTimestamp().toEpochMilli(), DateTimeZone.UTC);

        // Parse the Flow Log message
        KinesisLogEntry kinesisLogEntry = KinesisLogEntry.create(kinesisStreamName, logGroup, logStream, timestamp, new String(payloadBytes));
        return Collections.singletonList(toRawMessage().apply(kinesisLogEntry));
    }

    /**
     * @return A {@link org.graylog2.plugin.journal.RawMessage} containing a serialized
     * {@link org.graylog.integrations.aws.cloudwatch.KinesisLogEntry} object
     */
    private Function<KinesisLogEntry, org.graylog2.plugin.journal.RawMessage> toRawMessage() {
        return kle -> {
            try {
                return new RawMessage(objectMapper.writeValueAsBytes(kle));
            } catch (JsonProcessingException e) {
                LOG.error("Failed to encode message bytes.", e);
                return null;
            }
        };
    }

    private List<CloudWatchLogEvent> handleCompressedMessages(byte[] payloadBytes, String streamName) throws IOException {

        LOG.debug("The supplied payload is GZip compressed. Proceeding to decompress and parse as a CloudWatch log message.");

        final byte[] bytes = Tools.decompressGzip(payloadBytes).getBytes();
        LOG.debug("They payload was decompressed successfully. size [{}]", bytes.length);

        // Assume that the payload is from CloudWatch.
        // Extract messages, so that they can be committed to journal one by one.
        final CloudWatchLogSubscriptionData data = objectMapper.readValue(bytes, CloudWatchLogSubscriptionData.class);

        if (LOG.isTraceEnabled()) {
            // Log the number of events retrieved from CloudWatch. DO NOT log the content of the messages.
            LOG.trace("[{}] messages obtained from CloudWatch", data.logEvents().size());
        }

        return data.logEvents();
    }

    /**
     * Checks if the supplied stream is GZip compressed.
     *
     * @param bytes a byte array.
     * @return true if the byte array is GZip compressed and false if not.
     */
    public static boolean isCompressed(byte[] bytes) {
        if ((bytes == null) || (bytes.length < 2)) {
            return false;
        } else {

            // If the byte array is GZipped, then the first or second byte will be the GZip magic number.
            final boolean firstByteIsMagicNumber = bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC);
            // The >> operator shifts the GZIP magic number to the second byte.
            final boolean secondByteIsMagicNumber = bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> EIGHT_BITS);
            return firstByteIsMagicNumber && secondByteIsMagicNumber;
        }
    }
}
