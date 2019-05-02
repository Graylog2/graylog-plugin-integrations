package org.graylog.integrations.aws.resources;

import org.graylog.integrations.aws.AWSCloudWatchService;
import org.graylog.integrations.aws.AWSService;
import org.graylog.integrations.aws.resources.responses.AWSLogGroupsResponse;
import org.graylog.integrations.aws.resources.responses.AWSRegionResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AWSCloudWatchResourceTest {

    private AWSCloudWatchResource cloudWatchResource;

    @Before
    public void setUp() {
        cloudWatchResource = new AWSCloudWatchResource(new AWSService(), new AWSCloudWatchService());
    }

    @Test
    public void testGetRegions() {

        List<AWSRegionResponse> regions = cloudWatchResource.regions();
        assertFalse(regions.isEmpty());
    }

    @Test
    public void testGetLogGroups() {

        AWSLogGroupsResponse awsLogGroupsResponse = cloudWatchResource.logGroups("test-log-group-name");
        assertEquals(2, awsLogGroupsResponse.logGroupNames().size());
        assertTrue(awsLogGroupsResponse.success());
    }
}