package org.graylog.integrations.aws.resources;

import org.graylog.integrations.aws.CloudWatchService;
import org.graylog.integrations.aws.KinesisService;
import org.graylog.integrations.aws.AWSService;
import org.graylog.integrations.aws.resources.responses.KinesisStreamsResponse;
import org.graylog.integrations.aws.resources.responses.LogGroupsResponse;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AWSResourceTest {

    private AWSResource cloudWatchResource;

    @Before
    public void setUp() {
        cloudWatchResource = new AWSResource(new AWSService(), new CloudWatchService(), new KinesisService());
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
    public void testGetStreams() {

        KinesisStreamsResponse awsLogGroupsResponse = cloudWatchResource.kinesisStreams("test-region");
        assertEquals(2, awsLogGroupsResponse.streamNames().size());
        assertTrue(awsLogGroupsResponse.success());
    }
}