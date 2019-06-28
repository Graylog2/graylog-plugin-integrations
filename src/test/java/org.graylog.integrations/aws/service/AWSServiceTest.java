package org.graylog.integrations.aws.service;

import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.inputs.AWSInput;
import org.graylog.integrations.aws.resources.requests.AWSInputCreateRequest;
import org.graylog.integrations.aws.resources.responses.AWSRegion;
import org.graylog.integrations.aws.resources.responses.AvailableServiceResponse;
import org.graylog.integrations.aws.transports.KinesisTransport;
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
                                             true,
                                             60);
        awsService.saveInput(request, user);

        // Verify that inputService received a valid input to save.
        final ArgumentCaptor<InputCreateRequest> argumentCaptor = ArgumentCaptor.forClass(InputCreateRequest.class);
        verify(messageInputFactory, times(1)).create(argumentCaptor.capture(), eq("a-user-name"), eq("node-id"));

        // Just verify that the input create request was prepared correctly. This verifies the important argument
        // transposition logic.
        // It's too hard to mock the full inputService.save process, so we are not going to check the final resulting input.
        InputCreateRequest input = argumentCaptor.getValue();
        assertEquals("AWS Input", input.title());
        assertEquals(AWSInput.TYPE, input.type());
        assertFalse(input.global());
        assertEquals("us-east-1", input.configuration().get(AWSInput.CK_AWS_REGION));
        assertEquals("KINESIS_FLOW_LOGS", input.configuration().get(AWSInput.CK_AWS_MESSAGE_TYPE));
        assertEquals("a-key", input.configuration().get(AWSInput.CK_ACCESS_KEY));
        assertEquals("a-secret", input.configuration().get(AWSInput.CK_SECRET_KEY));
        assertEquals("An AWS Input", input.configuration().get(AWSInput.CK_DESCRIPTION));
        assertEquals("us-east-1", input.configuration().get(AWSInput.CK_AWS_REGION));
        assertEquals("AWS Input", input.configuration().get(MessageInput.FIELD_TITLE));
        assertEquals("a-stream", input.configuration().get(KinesisTransport.CK_KINESIS_STREAM_NAME));
        assertEquals(60, input.configuration().get(KinesisTransport.CK_KINESIS_MAX_THROTTLED_WAIT_MS));
        assertEquals(10000, input.configuration().get(KinesisTransport.CK_KINESIS_RECORD_BATCH_SIZE));
    }

    @Test
    public void regionTest() {

        List<AWSRegion> regions = awsService.getAvailableRegions().regions();

        // Use a loop presence check.
        // Check format of random region.
        boolean foundEuWestRegion = false;
        for (AWSRegion availableAWSRegion : regions) {

            if (availableAWSRegion.regionId().equals("eu-west-2")) {
                foundEuWestRegion = true;
            }
        }
        assertTrue(foundEuWestRegion);

        // Use one liner presence checks.
        assertTrue(regions.stream().anyMatch(r -> r.displayValue().equals("EU (Stockholm): eu-north-1")));
        assertEquals("There should be 20 total regions. This will change in future versions of the AWS SDK", 20, regions.size());
    }

    @Test
    public void testAvailableServices() {

        AvailableServiceResponse services = awsService.getAvailableServices();

        // There should be one service.
        assertEquals(1, services.total());
        assertEquals(1, services.services().size());

        // CloudWatch should be in the list of available services.
        assertTrue(services.services().stream().anyMatch(s -> s.name().equals("CloudWatch")));
    }
}