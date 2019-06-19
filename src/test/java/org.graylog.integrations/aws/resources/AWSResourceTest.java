package org.graylog.integrations.aws.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.aws.service.KinesisService;
import org.graylog.integrations.aws.service.CloudWatchService;
import org.graylog.integrations.aws.service.AWSService;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;

import java.util.HashMap;

import static org.mockito.Mockito.when;

public class AWSResourceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private AWSResource awsResource;

    @Mock
    private KinesisClientBuilder kinesisClientBuilder;

    @Mock
    private CloudWatchLogsClientBuilder logsClientBuilder;

    @Mock
    KinesisClient kinesisClient;

    @Before
    public void setUp() {

        // Make sure that the actual KinesisClient is not used.
        when(kinesisClientBuilder.region(Region.EU_WEST_1)).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        // Set up the chain of mocks.
        awsResource = new AWSResource(new AWSService(), new KinesisService(kinesisClientBuilder,
                                                                           new ObjectMapper(), new HashMap<>()), new CloudWatchService(logsClientBuilder));
    }
}