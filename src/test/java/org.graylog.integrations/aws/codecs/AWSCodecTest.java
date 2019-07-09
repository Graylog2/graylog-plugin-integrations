package org.graylog.integrations.aws.codecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.AWSTestingUtils;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.cloudwatch.KinesisLogEntry;
import org.graylog.integrations.aws.inputs.AWSInput;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.journal.RawMessage;
import org.graylog2.shared.bindings.providers.ObjectMapperProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class AWSCodecTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapperProvider().get();
    }

    @Test
    public void testKinesisFlowLogCodec() throws JsonProcessingException {

        final HashMap<String, Object> configMap = new HashMap<>();
        configMap.put(AWSInput.CK_AWS_MESSAGE_TYPE, AWSMessageType.KINESIS_FLOW_LOGS.toString());
        final Configuration configuration = new Configuration(configMap);
        final AWSCodec codec = new AWSCodec(configuration, AWSTestingUtils.buildAWSCodecs());

        final DateTime timestamp = DateTime.now(DateTimeZone.UTC);
        final KinesisLogEntry kinesisLogEntry = KinesisLogEntry.create("a-stream", "log-group", "log-stream", timestamp,
                                                                 "2 423432432432 eni-3244234 172.1.1.2 172.1.1.2 80 2264 6 1 52 1559738144 1559738204 ACCEPT OK");

        Message message = codec.decode(new RawMessage(objectMapper.writeValueAsBytes(kinesisLogEntry)));
        Assert.assertEquals("log-group", message.getField(AbstractKinesisCodec.FIELD_LOG_GROUP));
        Assert.assertEquals("log-stream", message.getField(AbstractKinesisCodec.FIELD_LOG_STREAM));
        Assert.assertEquals("a-stream", message.getField(AbstractKinesisCodec.FIELD_KINESIS_STREAM));
        Assert.assertEquals(6, message.getField(KinesisCloudWatchFlowLogCodec.FIELD_PROTOCOL_NUMBER));
        Assert.assertEquals("172.1.1.2", message.getField(KinesisCloudWatchFlowLogCodec.FIELD_SRC_ADDR));
        Assert.assertEquals(KinesisCloudWatchFlowLogCodec.SOURCE, message.getField("source"));
        Assert.assertEquals("eni-3244234 ACCEPT TCP 172.1.1.2:80 -> 172.1.1.2:2264", message.getField("message"));
        Assert.assertEquals(1L, message.getField(KinesisCloudWatchFlowLogCodec.FIELD_PACKETS));
        Assert.assertEquals(80, message.getField(KinesisCloudWatchFlowLogCodec.FIELD_SRC_PORT));
        Assert.assertEquals(60, message.getField(KinesisCloudWatchFlowLogCodec.FIELD_CAPTURE_WINDOW_DURATION));
        Assert.assertEquals("TCP", message.getField(KinesisCloudWatchFlowLogCodec.FIELD_PROTOCOL));
        Assert.assertEquals("423432432432", message.getField(KinesisCloudWatchFlowLogCodec.FIELD_ACCOUNT_ID));
        Assert.assertEquals("eni-3244234", message.getField(KinesisCloudWatchFlowLogCodec.FIELD_INTERFACE_ID));
        Assert.assertEquals("OK", message.getField(KinesisCloudWatchFlowLogCodec.FIELD_LOG_STATUS));
        Assert.assertEquals(52L, message.getField(KinesisCloudWatchFlowLogCodec.FIELD_BYTES));
        Assert.assertEquals(true, message.getField(KinesisCloudWatchFlowLogCodec.SOURCE_GROUP_IDENTIFIER));
        Assert.assertEquals("172.1.1.2", message.getField(KinesisCloudWatchFlowLogCodec.FIELD_DST_ADDR));
        Assert.assertEquals(2264, message.getField(KinesisCloudWatchFlowLogCodec.FIELD_DST_PORT));
        Assert.assertEquals("ACCEPT", message.getField(KinesisCloudWatchFlowLogCodec.FIELD_ACTION));
        Assert.assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    public void testKinesisRawCodec() throws JsonProcessingException {

        final HashMap<String, Object> configMap = new HashMap<>();
        configMap.put(AWSInput.CK_AWS_MESSAGE_TYPE, AWSMessageType.KINESIS_RAW.toString());
        final Configuration configuration = new Configuration(configMap);
        final AWSCodec codec = new AWSCodec(configuration, AWSTestingUtils.buildAWSCodecs());

        final DateTime timestamp = DateTime.now(DateTimeZone.UTC);
        final KinesisLogEntry kinesisLogEntry = KinesisLogEntry.create("a-stream", "log-group", "log-stream", timestamp,
                                                                 "This a raw message");

        Message message = codec.decode(new RawMessage(objectMapper.writeValueAsBytes(kinesisLogEntry)));
        Assert.assertEquals("log-group", message.getField(AbstractKinesisCodec.FIELD_LOG_GROUP));
        Assert.assertEquals("log-stream", message.getField(AbstractKinesisCodec.FIELD_LOG_STREAM));
        Assert.assertEquals("a-stream", message.getField(AbstractKinesisCodec.FIELD_KINESIS_STREAM));
        Assert.assertEquals(KinesisRawLogCodec.SOURCE, message.getField("source"));
        Assert.assertEquals("This a raw message", message.getField("message"));
        Assert.assertEquals(timestamp, message.getTimestamp());
    }
}