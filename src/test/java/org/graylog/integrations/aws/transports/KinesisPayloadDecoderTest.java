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
                                                   AWSMessageType.KINESIS_FLOW_LOGS,
                                                   "a-stream");

        rawDecoder = new KinesisPayloadDecoder(new ObjectMapperProvider().get(),
                                               AWSMessageType.KINESIS_RAW,
                                               "a-stream");
    }

    @Test
    public void testCloudWatchFlowLogDecoding() throws IOException {

        final List<KinesisLogEntry> logEntries =
                flowLogDecoder.processMessages(AWSTestingUtils.buildCloudWatchFlowLogPayload(), Instant.now());

        Assert.assertEquals(2, logEntries.size());
        // Verify that there are two flow logs present in the parsed result.
        Assert.assertEquals(2, logEntries.stream().filter(logEntry -> {
            final AWSLogMessage logMessage = new AWSLogMessage(logEntry.message());
            return logMessage.isFlowLog();
        }).count());
    }

    @Test
    public void testCloudWatchRawDecoding() throws IOException {

        final List<KinesisLogEntry> logEntries =
                flowLogDecoder.processMessages(AWSTestingUtils.buildCloudWatchRawPayload(), Instant.now());

        Assert.assertEquals(2, logEntries.size());
        // Verify that there are two flow logs present in the parsed result.
        Assert.assertEquals(2, logEntries.stream().filter(logEntry -> {
            final AWSLogMessage logMessage = new AWSLogMessage(logEntry.message());
            return logMessage.detectLogMessageType() == AWSMessageType.KINESIS_RAW;
        }).count());
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
        final KinesisLogEntry resultLogEntry = logEntries.stream()
                                                         .filter(logEntry -> logEntry.message().equals(textLogMessage))
                                                         .findAny().get();
        // Verify that the timestamp matches.
        Assert.assertEquals(new DateTime(now.toEpochMilli(), DateTimeZone.UTC), resultLogEntry.timestamp());
    }
}