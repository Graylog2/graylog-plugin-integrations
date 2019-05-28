package org.graylog.integrations.aws;

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

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

public class AWSClientTest {

    private static final String[] TWO_TEST_STREAMS = {"test-stream-1", "test-stream-2"};
    private static final String TEST_REGION = Region.EU_WEST_1.id();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private KinesisClientBuilder kinesisClientBuilder;

    @Mock
    private KinesisClient kinesisClient;

    private AWSClient awsClient;

    @Before
    public void setUp() throws Exception {

        // Create an AWS client with a mock KinesisClientBuilder
        awsClient = new AWSClient(kinesisClientBuilder);
    }

    @Test
    public void name() {

        Main.main(null);
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
        List<String> kinesisStreams = awsClient.getKinesisStreams(TEST_REGION, null, null);
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

        kinesisStreams = awsClient.getKinesisStreams(TEST_REGION, null, null);

        // There should be 4 total streams (two from each page).
        assertEquals(4, kinesisStreams.size());
    }
}