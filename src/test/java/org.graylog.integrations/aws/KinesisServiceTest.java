package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.responses.KinesisStreamsResponse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KinesisServiceTest {

    private KinesisService kinesisService;

    @Before
    public void setUp() {
        kinesisService = new KinesisService();
    }

    @Test
    public void testGetStreams() {

        KinesisStreamsResponse streamsResponse = kinesisService.getKinesisStreams("test-region", null, null);

        assertTrue(streamsResponse.success());
        assertEquals(2, streamsResponse.streamNames().size());
    }
}