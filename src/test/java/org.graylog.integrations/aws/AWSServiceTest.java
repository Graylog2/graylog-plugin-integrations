package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AWSServiceTest {

    AWSService awsService;

    @Before
    public void setUp() throws Exception {
        awsService = new AWSService();
    }

    @Test
    public void testLogIdentification() {

        // Verify that an ACCEPT flow log us detected as a flow log.
        AWSLogMessage logMessage = new AWSLogMessage("2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 ACCEPT OK");
        assertEquals(AWSLogMessage.Type.FLOW_LOGS, logMessage.messageType());

        // Verify that an ACCEPT flow log us detected as a flow log.
        logMessage = new AWSLogMessage("2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 REJECT OK");
        assertEquals(AWSLogMessage.Type.FLOW_LOGS, logMessage.messageType());

        // Verify that it's detected as unknown
        logMessage = new AWSLogMessage("haha this is not a real log message");
        assertEquals(AWSLogMessage.Type.UNKNOWN, logMessage.messageType());
    }

    @Test
    public void healthCheck() {

        KinesisHealthCheckRequest request = KinesisHealthCheckRequest.create("us-east-1", "some-group", "", "");
        KinesisHealthCheckResponse healthCheckResponse = awsService.healthCheck(request);

        // Hard-coded to flow logs for now. This will be mocked out with a real message at some point
        assertEquals(AWSLogMessage.Type.FLOW_LOGS.toString(), healthCheckResponse.logType());
    }

    @Test
    public void regionTest() {

        List<RegionResponse> availableRegions = awsService.getAvailableRegions();

        // Check format of random region.
        assertTrue(availableRegions.stream().anyMatch(r -> r.regionId().equals("eu-west-2")));
        assertTrue(availableRegions.stream().anyMatch(r -> r.regionDescription().equals("EU (Stockholm)")));
        assertTrue(availableRegions.stream().anyMatch(r -> r.displayValue().equals("EU (Stockholm): eu-north-1")));
        assertEquals("There should be 20 total regions. This will change in future versions of the AWS SDK",
                     20, availableRegions.size());
    }
}