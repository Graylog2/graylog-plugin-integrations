package org.graylog.integrations.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.aws.codec.CloudWatchFlowLogCodec;
import org.graylog.integrations.aws.codec.CloudWatchRawLogCodec;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.service.AWSLogMessage;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.ListStreamsRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

public class KinesisServiceTest {

    private static final String TEST_STREAM_1 = "test-stream-1";
    private static final String TEST_STREAM_2 = "test-stream-2";
    private static final String[] TWO_TEST_STREAMS = {TEST_STREAM_1, TEST_STREAM_2};
    private static final String TEST_REGION = Region.EU_WEST_1.id();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private KinesisClientBuilder kinesisClientBuilder;

    @Mock
    private KinesisClient kinesisClient;

    private KinesisService kinesisService;

    private Map<String, Codec.Factory<? extends Codec>> availableCodecs;

    @Before
    public void setUp() {

        // Create an AWS client with a mock KinesisClientBuilder
        availableCodecs = new HashMap<>();

        // Prepare test codecs. These have to be manually instantiated for the test context.
        availableCodecs.put(CloudWatchRawLogCodec.NAME, new CloudWatchRawLogCodec.Factory() {
            @Override
            public CloudWatchRawLogCodec create(Configuration configuration) {
                return new CloudWatchRawLogCodec(configuration, new ObjectMapper());
            }

            @Override
            public CloudWatchRawLogCodec.Config getConfig() {
                return null;
            }

            @Override
            public Codec.Descriptor getDescriptor() {
                return null;
            }
        });

        availableCodecs.put(CloudWatchFlowLogCodec.NAME, new CloudWatchFlowLogCodec.Factory() {
            @Override
            public CloudWatchFlowLogCodec create(Configuration configuration) {
                return new CloudWatchFlowLogCodec(configuration, new ObjectMapper());
            }

            @Override
            public CloudWatchFlowLogCodec.Config getConfig() {
                return null;
            }

            @Override
            public Codec.Descriptor getDescriptor() {
                return null;
            }
        });

        kinesisService = new KinesisService(Configuration.EMPTY_CONFIGURATION, kinesisClientBuilder, new ObjectMapper(), availableCodecs);
    }

    @Test
    public void testLogIdentification() {

        // Verify that an ACCEPT flow log us detected as a flow log.
        AWSLogMessage logMessage = new AWSLogMessage("2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 ACCEPT OK");
        assertEquals(AWSLogMessage.Type.FLOW_LOGS, logMessage.detectLogMessageType());

        // Verify that an ACCEPT flow log us detected as a flow log.
        logMessage = new AWSLogMessage("2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 REJECT OK");
        assertEquals(AWSLogMessage.Type.FLOW_LOGS, logMessage.detectLogMessageType());

        // Verify that a message with 14 spaces (instead of 13) is not identified as a flow log.
        logMessage = new AWSLogMessage("2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 REJECT OK ONE-MORE-WORD");
        assertEquals(AWSLogMessage.Type.UNKNOWN, logMessage.detectLogMessageType());

        // Verify that a message with 12 spaces (instead of 13) is not identified as a flow log.
        logMessage = new AWSLogMessage("2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 REJECT");
        assertEquals(AWSLogMessage.Type.UNKNOWN, logMessage.detectLogMessageType());

        // Verify that it's detected as unknown
        logMessage = new AWSLogMessage("haha this is not a real log message");
        assertEquals(AWSLogMessage.Type.UNKNOWN, logMessage.detectLogMessageType());
    }

    @Test
    public void healthCheck() throws ExecutionException, IOException {

        // TODO: This test verifies the path that CloudWatch flow logs are being sent.
        //  Add a test for the case when an unknown CloudWatch format is sent, and also
        //  when a non-CloudWatch payload is provided.

        when(kinesisClientBuilder.region(Region.EU_WEST_1)).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        when(kinesisClient.listStreams(isA(ListStreamsRequest.class)))
                .thenReturn(ListStreamsResponse.builder()
                                               .streamNames(TWO_TEST_STREAMS)
                                               .hasMoreStreams(false).build());

        // TODO: Additional mock prep will be needed when reading from Kinesis is added.
        KinesisHealthCheckRequest request = KinesisHealthCheckRequest.create(Region.EU_WEST_1.id(),
                                                                             "", "", TEST_STREAM_1, "");
        KinesisHealthCheckResponse healthCheckResponse = kinesisService.healthCheck(request);

        // Hard-coded to flow logs for now. This will be mocked out with a real message at some point
        assertEquals(AWSLogMessage.Type.FLOW_LOGS.toString(), healthCheckResponse.logType());
    }

    @Test
    public void testGetStreams() throws ExecutionException {

        // Test with two streams and one page. This is the most common case for most AWS accounts.
        when(kinesisClientBuilder.region(Region.EU_WEST_1)).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        when(kinesisClient.listStreams(isA(ListStreamsRequest.class)))
                .thenReturn(ListStreamsResponse.builder()
                                               .streamNames(TWO_TEST_STREAMS)
                                               .hasMoreStreams(false).build());


        List<String> kinesisStreams = kinesisService.getKinesisStreams(TEST_REGION, null, null);
        assertEquals(2, kinesisStreams.size());

        // Test with stream paging functionality. This will be the case when a large number of Kinesis streams
        // are present on a particular AWS account.
        when(kinesisClientBuilder.region(Region.EU_WEST_1)).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        when(kinesisClient.listStreams(isA(ListStreamsRequest.class)))
                // First return a response with two streams indicating that there are more.
                .thenReturn(ListStreamsResponse.builder()
                                               .streamNames(TWO_TEST_STREAMS)
                                               .hasMoreStreams(true).build())
                // Then return a response with two streams and indicate that all have been retrieved.
                .thenReturn(ListStreamsResponse.builder()
                                               .streamNames(TWO_TEST_STREAMS)
                                               .hasMoreStreams(false).build()); // Indicate no more streams.

        kinesisStreams = kinesisService.getKinesisStreams(TEST_REGION, null, null);

        // There should be 4 total streams (two from each page).
        assertEquals(4, kinesisStreams.size());
    }
}