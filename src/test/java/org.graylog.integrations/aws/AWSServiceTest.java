package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.responses.AWSRegionResponse;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AWSServiceTest {

    @Test
    public void regionTest() {

        AWSService awsService = new AWSService();
        List<AWSRegionResponse> availableRegions = awsService.getAvailableRegions();

        // Check format of random region.
        assertTrue(availableRegions.stream().anyMatch(r -> r.regionId().equals("eu-west-2")));
        assertTrue(availableRegions.stream().anyMatch(r -> r.regionDescription().equals("EU (Stockholm)")));
        assertTrue(availableRegions.stream().anyMatch(r -> r.displayValue().equals("EU (Stockholm): eu-north-1")));
        assertEquals("There should be 20 total regions. This will change in future versions of the AWS SDK",
                     20, availableRegions.size());
    }
}