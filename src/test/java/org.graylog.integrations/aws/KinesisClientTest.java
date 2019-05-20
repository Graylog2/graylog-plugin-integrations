package org.graylog.integrations.aws;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
    public void testGetStreams() throws ExecutionException {

        List<String> kinesisStreams = kinesisClient.getKinesisStreams("test-region", null, null);
        assertEquals(2, kinesisStreams.size());
    }
}