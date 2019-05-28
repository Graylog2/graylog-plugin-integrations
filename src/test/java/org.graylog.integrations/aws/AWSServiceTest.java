package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.graylog.integrations.aws.service.AWSService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AWSServiceTest {

    private AWSService awsService;

    @Before
    public void setUp() {
        awsService = new AWSService();
    }

    @Test
    public void regionTest() {

        List<RegionResponse> availableRegions = awsService.getAvailableRegions();

        // Use a loop presence check.
        // Check format of random region.
        boolean foundEuWestRegion = false;
        for (RegionResponse availableRegion : availableRegions) {

            if (availableRegion.regionId().equals("eu-west-2")) {
                foundEuWestRegion = true;
            }
        }
        assertTrue(foundEuWestRegion);

        // Use one liner presence checks.
        assertTrue(availableRegions.stream().anyMatch(r -> r.regionDescription().equals("EU (Stockholm)")));
        assertTrue(availableRegions.stream().anyMatch(r -> r.displayValue().equals("EU (Stockholm): eu-north-1")));
        assertEquals("There should be 20 total regions. This will change in future versions of the AWS SDK",
                     20, availableRegions.size());
    }
}