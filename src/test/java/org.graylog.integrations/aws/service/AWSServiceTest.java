package org.graylog.integrations.aws.service;

import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.inputs.AWSInput;
import org.graylog.integrations.aws.resources.requests.AWSInputCreateRequest;
import org.graylog.integrations.aws.resources.responses.AvailableAWSServiceSummmary;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.graylog2.inputs.Input;
import org.graylog2.inputs.InputServiceImpl;
import org.graylog2.plugin.database.users.User;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.rest.models.system.inputs.requests.InputCreateRequest;
import org.graylog2.shared.inputs.MessageInputFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import software.amazon.awssdk.regions.Region;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AWSServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private AWSService awsService;

    @Mock
    private InputServiceImpl inputService;

    @Mock
    private User user;

    @Mock
    private NodeId nodeId;

    @Mock
    private MessageInput messageInput;

    @Mock
    MessageInputFactory messageInputFactory;

    @Before
    public void setUp() {

        awsService = new AWSService(inputService, messageInputFactory, nodeId);
    }

    @Test
    public void testSaveInput() throws Exception {
        when(nodeId.toString()).thenReturn("node-id");
        when(inputService.create(isA(HashMap.class))).thenCallRealMethod();
        when(inputService.save(isA(Input.class))).thenReturn("input-id");
        when(user.getName()).thenReturn("a-user-name");
        when(messageInputFactory.create(isA(InputCreateRequest.class), isA(String.class), isA(String.class))).thenReturn(messageInput);

        AWSInputCreateRequest request =
                AWSInputCreateRequest.create("AWS Input",
                                             "An AWS Input",
                                             AWSMessageType.KINESIS_FLOW_LOGS.toString(),
                                             "a-key", "a-secret",
                                             "a-stream",
                                             Region.US_EAST_1.id(),
                                             10000,
                                             "",
                                             false,
                                             true);
        awsService.saveInput(request, user);

        // Verify that inputService received a valid input to save.
        final ArgumentCaptor<InputCreateRequest> argumentCaptor = ArgumentCaptor.forClass(InputCreateRequest.class);
        verify(messageInputFactory, times(1)).create(argumentCaptor.capture(), eq("a-user-name"), eq("node-id"));

        // Just verify that the input create request was prepared correctly.
        // It's too hard to mock the full inputService.save process.
        InputCreateRequest input = argumentCaptor.getValue();
        assertEquals("AWS Input", input.title());
        assertEquals(AWSInput.TYPE, input.type());
        assertFalse(input.global());
        assertEquals("us-east-1", input.configuration().get("aws_region"));
        assertEquals("KINESIS_FLOW_LOGS", input.configuration().get("aws_input_type"));
        assertEquals("a-key", input.configuration().get("aws_access_key"));
        assertEquals("a-secret", input.configuration().get("aws_secret_key"));
        assertEquals("a-stream", input.configuration().get("kinesis_stream_name"));
        assertEquals(true, input.configuration().get("kinesis_max_throttled_wait"));
        assertEquals("An AWS Input", input.configuration().get("description"));
        assertEquals("us-east-1", input.configuration().get("global"));
        assertEquals("AWS Input", input.configuration().get("title"));
        assertEquals(10000, input.configuration().get("kinesis_record_batch_size"));

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

    @Test
    public void testAvailableServices() {

        AvailableAWSServiceSummmary services = awsService.getAvailableServices();

        // There should be one service.
        assertEquals(1, services.total());
        assertEquals(1, services.services().size());

        // CloudWatch should be in the list of available services.
        assertTrue(services.services().stream().anyMatch(s -> s.name().equals("CloudWatch")));
    }
}