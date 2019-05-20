package org.graylog.integrations.aws.resources;

import org.graylog.integrations.aws.AWSService;
import org.graylog.integrations.aws.CloudWatchService;
import org.graylog.integrations.aws.AWSKinesisClient;
import org.graylog.integrations.aws.resources.responses.LogGroupsResponse;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AWSResourceTest {

    private AWSResource cloudWatchResource;

    @Before
    public void setUp() {
        cloudWatchResource = new AWSResource(new AWSService(), new CloudWatchService(), new AWSKinesisClient());
    }

    @Test
    public void testGetRegions() {

        List<RegionResponse> regions = cloudWatchResource.regions();
        assertFalse(regions.isEmpty());
    }

    @Test
    public void testGetLogGroups() {

        LogGroupsResponse logGroupsResponse = cloudWatchResource.logGroups("test-region");
        assertEquals(2, logGroupsResponse.logGroupNames().size());
        assertTrue(logGroupsResponse.success());
    }

    @Test
    public void testGetStreams() throws ExecutionException {

        List<String> kinesisStreams = cloudWatchResource.kinesisStreams("eu-west-1");
        assertEquals(2, kinesisStreams.size());
    }
}