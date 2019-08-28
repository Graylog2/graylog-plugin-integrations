package org.graylog.integrations.aws.transports;

import org.graylog.integrations.aws.AWSLogMessage;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.AWSTestingUtils;
import org.graylog.integrations.aws.cloudwatch.KinesisLogEntry;
import org.graylog2.shared.bindings.providers.ObjectMapperProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class KinesisPayloadDecoderTest {

    private KinesisPayloadDecoder flowLogDecoder;
    private KinesisPayloadDecoder rawDecoder;

    @Before
    public void setUp() {

        flowLogDecoder = new KinesisPayloadDecoder(new ObjectMapperProvider().get(),
                                                   AWSMessageType.KINESIS_CLOUDWATCH_FLOW_LOGS,
                                                   "a-stream");

        rawDecoder = new KinesisPayloadDecoder(new ObjectMapperProvider().get(),
                                               AWSMessageType.KINESIS_RAW,
                                               "a-stream");
    }

    @Test
    public void testCloudWatchFlowLogDecoding() throws IOException {

        final List<KinesisLogEntry> logEntries =
                flowLogDecoder.processMessages(AWSTestingUtils.cloudWatchFlowLogPayload(),
                                               Instant.ofEpochMilli(AWSTestingUtils.CLOUD_WATCH_TIMESTAMP.getMillis()));

        Assert.assertEquals(2, logEntries.size());

        // Verify that there are two flowlogs present in the parsed result.
        Assert.assertEquals(2, logEntries.stream().filter(logEntry -> {
            final AWSLogMessage logMessage = new AWSLogMessage(logEntry.message());
            return logMessage.isFlowLog();
        }).count());

        // Verify that both messages have to correct timestamp.
        Assert.assertEquals(2, logEntries.stream()
                                         .filter(logEntry -> logEntry.timestamp().equals(AWSTestingUtils.CLOUD_WATCH_TIMESTAMP))
                                         .count());
    }

    @Test
    public void testCloudWatchRawDecoding() throws IOException {

        final List<KinesisLogEntry> logEntries =
                flowLogDecoder.processMessages(AWSTestingUtils.cloudWatchRawPayload(), Instant.now());

        Assert.assertEquals(2, logEntries.size());
        // Verify that there are two flow logs present in the parsed result.
        Assert.assertEquals(2, logEntries.stream().filter(logEntry -> {
            final AWSLogMessage logMessage = new AWSLogMessage(logEntry.message());
            return logMessage.detectLogMessageType(true) == AWSMessageType.KINESIS_CLOUDWATCH_RAW;
        }).count());

        // Verify that both messages have to correct timestamp.
        Assert.assertEquals(2, logEntries.stream()
                                         .filter(logEntry -> logEntry.timestamp().equals(AWSTestingUtils.CLOUD_WATCH_TIMESTAMP))
                                         .count());
    }

    @Test
    public void testKinesisRawDecoding() throws IOException {

        final String textLogMessage = "a text log message";
        final Instant now = Instant.now();
        final List<KinesisLogEntry> logEntries =
                rawDecoder.processMessages(textLogMessage.getBytes(), now);

        Assert.assertEquals(1, logEntries.size());
        // Verify that there are two flow logs present in the parsed result.
        Assert.assertEquals(1, logEntries.stream().filter(logEntry -> logEntry.message().equals(textLogMessage)).count());

        // Verify timestamp and message contents.
        final KinesisLogEntry resultLogEntry = logEntries.stream().findAny().get();
        Assert.assertEquals(textLogMessage, resultLogEntry.message());
        Assert.assertEquals(new DateTime(now.toEpochMilli(), DateTimeZone.UTC), resultLogEntry.timestamp());
    }
}