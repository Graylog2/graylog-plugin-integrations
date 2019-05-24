package org.graylog.integrations.aws.resources;

import org.graylog.integrations.aws.AWSKinesisClient;
import org.graylog.integrations.aws.AWSService;
import org.graylog.integrations.aws.CloudWatchService;
import org.graylog.integrations.aws.resources.responses.LogGroupsResponse;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

public class AWSResourceTest {

    private static final String[] TWO_TEST_STREAMS = {"test-stream-1", "test-stream-2"};
    private static final String TEST_REGION = Region.EU_WEST_1.id();

    private AWSResource awsResource;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private KinesisClientBuilder kinesisClientBuilder;

    @Mock
    private KinesisClient kinesisClient;

    @Before
    public void setUp() {
        awsResource = new AWSResource(new AWSService(),
                                      new CloudWatchService(),
                                      new AWSKinesisClient(kinesisClientBuilder));
    }

    @Test
    public void testGetRegions() {

        List<RegionResponse> regions = awsResource.regions();
        assertFalse(regions.isEmpty());
    }

    @Test
    public void testGetLogGroups() {

        LogGroupsResponse logGroupsResponse = awsResource.logGroups("test-region");
        assertEquals(2, logGroupsResponse.logGroupNames().size());
        assertTrue(logGroupsResponse.success());
    }

    /**
     * Verifies that a list of streams can be obtained. Also tests pagination handling logic.
     */
    @Test
    public void testGetStreams() throws ExecutionException {

        // Test with two streams and one page. This is the most common case for most AWS accounts.
        when(kinesisClientBuilder.region(Region.EU_WEST_1)).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        when(kinesisClient.listStreams(isA(ListStreamsRequest.class)))
                .thenReturn(ListStreamsResponse.builder()
                                               .streamNames(TWO_TEST_STREAMS)
                                               .hasMoreStreams(false).build());
        List<String> kinesisStreams = awsResource.kinesisStreams(TEST_REGION);
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

        kinesisStreams = awsResource.kinesisStreams(TEST_REGION);

        // There should be 4 total streams (two from each page).
        assertEquals(4, kinesisStreams.size());
    }
}