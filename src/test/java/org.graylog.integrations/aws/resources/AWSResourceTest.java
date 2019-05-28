package org.graylog.integrations.aws.resources;

import org.graylog.integrations.aws.CloudWatchService;
import org.graylog.integrations.aws.KinesisService;
import org.graylog.integrations.aws.service.AWSService;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;

import static org.mockito.Mockito.when;

public class AWSResourceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private AWSResource awsResource;

    @Mock
    private KinesisClientBuilder kinesisClientBuilder;

    @Mock
    KinesisClient kinesisClient;

    @Before
    public void setUp() {

        // Make sure that the actual KinesisClient is not used.
        when(kinesisClientBuilder.region(Region.EU_WEST_1)).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        // Set up the chain of mocks.
        awsResource = new AWSResource(new AWSService(), new KinesisService(kinesisClientBuilder), new CloudWatchService());
    }
}