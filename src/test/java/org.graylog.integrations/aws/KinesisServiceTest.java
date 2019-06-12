package org.graylog.integrations.aws;

import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ThrowableAssert;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.service.AWSLogMessage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.ListStreamsRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

public class KinesisServiceTest {

    private static final String[] TWO_TEST_STREAMS = {"test-stream-1", "test-stream-2"};
    private static final String TEST_REGION = Region.EU_WEST_1.id();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private KinesisClientBuilder kinesisClientBuilder;

    @Test
    public void testGetStreamsCredentials() {
        AssertionsForClassTypes.assertThatThrownBy(() -> kinesisService.getKinesisStreams(TEST_REGION, "", ""))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("An AWS access key is required");
        AssertionsForClassTypes.assertThatThrownBy(() -> kinesisService.getKinesisStreams(TEST_REGION, "dsfadsdf", ""))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("An AWS secret key is required");
        AssertionsForClassTypes.assertThatThrownBy(() -> kinesisService.getKinesisStreams(TEST_REGION, "", "dsfadsdf"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("An AWS access key is required");
    }

    @Test
    public void testGetStreams() throws ExecutionException {

        // Test with two streams and one page. This is the most common case for most AWS accounts.
        when(kinesisClientBuilder.region(isA(Region.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.credentialsProvider(isA(AwsCredentialsProvider.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        when(kinesisClient.listStreams(isA(ListStreamsRequest.class)))
                .thenReturn(ListStreamsResponse.builder()
                                    .streamNames(TWO_TEST_STREAMS)
                                    .hasMoreStreams(false).build());


        List<String> kinesisStreams = kinesisService.getKinesisStreams(TEST_REGION, "accessKey", "secretKey");
        assertEquals(2, kinesisStreams.size());

        // Test with stream paging functionality. This will be the case when a large number of Kinesis streams
        // are present on a particular AWS account.
        when(kinesisClientBuilder.region(isA(Region.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.credentialsProvider(isA(AwsCredentialsProvider.class))).thenReturn(kinesisClientBuilder);
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

        kinesisStreams = kinesisService.getKinesisStreams(TEST_REGION, "accessKey", "secretKey");

        // There should be 4 total streams (two from each page).
        assertEquals(4, kinesisStreams.size());

    }

    // TODO Add retrieveKinesisLogs test

    @Mock
    private KinesisClient kinesisClient;

    private KinesisService kinesisService;

    @Before
    public void setUp() {

        // Create an AWS client with a mock KinesisClientBuilder
        kinesisService = new KinesisService(kinesisClientBuilder);
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
        KinesisHealthCheckResponse healthCheckResponse = kinesisService.healthCheck(request);

        // Hard-coded to flow logs for now. This will be mocked out with a real message at some point
        assertEquals(AWSLogMessage.Type.FLOW_LOGS.toString(), healthCheckResponse.logType());
    }
}