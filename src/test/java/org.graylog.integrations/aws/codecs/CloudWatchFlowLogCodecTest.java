package org.graylog.integrations.aws.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.aws.cloudwatch.KinesisLogEntry;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CloudWatchFlowLogCodecTest {

    private KinesisCloudWatchFlowLogCodec codec;

    @Before
    public void setUp() {

        this.codec = new KinesisCloudWatchFlowLogCodec(Configuration.EMPTY_CONFIGURATION, new ObjectMapper());
    }

    /**
     * Verify that the correct values are parsed by the Flow Log codec.
     */
    @Test
    public void testFlowLogCodecValues() {

        String flowLogMessage = "2 423432432432 eni-3244234 172.1.1.2 172.1.1.2 80 2264 6 1 52 1559738144 1559738204 ACCEPT OK";
        final KinesisLogEntry logEvent = KinesisLogEntry.create("kinesisStream", "logGroup", "logStream", DateTime.now().getMillis() / 1000, flowLogMessage);
        Message message = codec.decodeLogData(logEvent);

        Assert.assertEquals("logGroup", message.getField("aws_log_group"));
        Assert.assertEquals("logStream", message.getField("aws_log_stream"));
        Assert.assertEquals(6, message.getField("protocol_number"));
        Assert.assertEquals("172.1.1.2", message.getField("src_addr"));
        Assert.assertEquals("aws-flowlogs", message.getField("source"));
        Assert.assertEquals("eni-3244234 ACCEPT TCP 172.1.1.2:80 -> 172.1.1.2:2264", message.getField("message"));
        Assert.assertEquals(1L, message.getField("packets"));
        Assert.assertEquals(80, message.getField("src_port"));
        Assert.assertEquals(60, message.getField("capture_window_duration_seconds"));
        Assert.assertEquals("TCP", message.getField("protocol"));
        Assert.assertEquals("423432432432", message.getField("account_id"));
        Assert.assertEquals("eni-3244234", message.getField("interface_id"));
        Assert.assertEquals("OK", message.getField("log_status"));
        Assert.assertEquals(52L, message.getField("bytes"));
        Assert.assertEquals(true, message.getField("aws_source"));
        Assert.assertEquals("172.1.1.2", message.getField("dst_addr"));
        Assert.assertEquals(2264, message.getField("dst_port"));
        Assert.assertEquals("ACCEPT", message.getField("action"));
        Assert.assertNotNull(message.getTimestamp());
    }
}
