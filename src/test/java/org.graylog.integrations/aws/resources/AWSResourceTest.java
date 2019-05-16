package org.graylog.integrations.aws.resources;

import org.graylog.integrations.aws.AWSCloudWatchService;
import org.graylog.integrations.aws.AWSKinesisService;
import org.graylog.integrations.aws.AWSService;
import org.graylog.integrations.aws.resources.responses.AWSKinesisStreamsResponse;
import org.graylog.integrations.aws.resources.responses.AWSLogGroupsResponse;
import org.graylog.integrations.aws.resources.responses.AWSRegionResponse;
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
        cloudWatchResource = new AWSResource(new AWSService(), new AWSCloudWatchService(), new AWSKinesisService());
    }

    @Test
    public void testGetRegions() {

        List<AWSRegionResponse> regions = cloudWatchResource.regions();
        assertFalse(regions.isEmpty());
    }

    @Test
    public void testGetLogGroups() {

        AWSLogGroupsResponse awsLogGroupsResponse = cloudWatchResource.logGroups("test-region");
        assertEquals(2, awsLogGroupsResponse.logGroupNames().size());
        assertTrue(awsLogGroupsResponse.success());
    }

    @Test
    public void testGetStreams() {

        AWSKinesisStreamsResponse awsLogGroupsResponse = cloudWatchResource.kinesisStreams("test-region");
        assertEquals(2, awsLogGroupsResponse.streamNames().size());
        assertTrue(awsLogGroupsResponse.success());
    }
}