package org.graylog.integrations.aws.resources;

import org.graylog.integrations.aws.AWSClient;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.graylog.integrations.aws.service.AWSService;
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
import static org.mockito.ArgumentMatchers.isA;
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
        awsResource = new AWSResource(new AWSService(new AWSClient(kinesisClientBuilder)));
    }
}