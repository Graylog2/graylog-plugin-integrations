package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.responses.KinesisStreamsResponse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KinesisClientTest {

    private KinesisClient kinesisClient;

    @Before
    public void setUp() {
        kinesisClient = new KinesisClient();
    }

    @Test
    public void testGetStreams() {

        KinesisStreamsResponse streamsResponse = kinesisClient.getKinesisStreams("test-region", null, null);

        assertTrue(streamsResponse.success());
        assertEquals(2, streamsResponse.streamNames().size());
    }
}