package org.graylog.integrations.aws.resources;

import org.graylog.integrations.aws.resources.requests.CreateLogSubscriptionRequest;
import org.graylog.integrations.aws.resources.requests.CreateRolePermissionRequest;
import org.graylog.integrations.aws.resources.requests.KinesisNewStreamRequest;
import org.graylog.integrations.aws.resources.responses.CreateLogSubscriptionResponse;
import org.graylog.integrations.aws.resources.responses.CreateRolePermissionResponse;
import org.graylog.integrations.aws.resources.responses.KinesisNewStreamResponse;
import org.graylog.integrations.aws.service.CloudWatchService;
import org.graylog.integrations.aws.service.KinesisService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterResponse;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.IamClientBuilder;
import software.amazon.awssdk.services.iam.model.CreateRoleResponse;
import software.amazon.awssdk.services.iam.model.GetRoleResponse;
import software.amazon.awssdk.services.iam.model.PutRolePolicyResponse;
import software.amazon.awssdk.services.iam.model.Role;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.CreateStreamResponse;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.StreamDescription;
import software.amazon.awssdk.services.kinesis.model.StreamStatus;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

/**
 * Integration test for all automatic setup requests/responses.
 */
public class KinesisSetupResourceTest {

    private static final String REGION = "us-east-1";
    private static final String KEY = "key";
    private static final String SECRET = "secret";
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private KinesisSetupResource setupResource;

    private CloudWatchService cloudWatchService;
    private KinesisService kinesisService;

    @Mock
    private IamClientBuilder iamClientBuilder;

    @Mock
    private IamClient iamClient;

    @Mock
    private CloudWatchLogsClientBuilder logsClientBuilder;

    @Mock
    private CloudWatchLogsClient logsClient;

    @Mock
    private KinesisClientBuilder kinesisClientBuilder;

    @Mock
    private KinesisClient kinesisClient;

    @Before
    public void setUp() throws Exception {

        // Set up services.
        cloudWatchService = new CloudWatchService(logsClientBuilder);
        kinesisService = new KinesisService(iamClientBuilder, kinesisClientBuilder, null, null);
        setupResource = new KinesisSetupResource(cloudWatchService, kinesisService);


        // Set up IAM client.
        when(iamClientBuilder.region(isA(Region.class))).thenReturn(iamClientBuilder);
        when(iamClientBuilder.credentialsProvider(isA(AwsCredentialsProvider.class))).thenReturn(iamClientBuilder);
        when(iamClientBuilder.build()).thenReturn(iamClient);

        // Set up CloudWatch client.
        when(logsClientBuilder.region(isA(Region.class))).thenReturn(logsClientBuilder);
        when(logsClientBuilder.credentialsProvider(isA(AwsCredentialsProvider.class))).thenReturn(logsClientBuilder);
        when(logsClientBuilder.build()).thenReturn(logsClient);

        // Set up Kinesis client.
        when(kinesisClientBuilder.region(isA(Region.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.credentialsProvider(isA(AwsCredentialsProvider.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        // Stream AWS request mocks
        when(kinesisClient.createStream(isA(CreateStreamRequest.class)))
                .thenReturn(CreateStreamResponse.builder().build());
        when(kinesisClient.describeStream(isA(Consumer.class)))
                .thenReturn(DescribeStreamResponse.builder()
                                                  .streamDescription(StreamDescription.builder()
                                                                                      .streamARN("stream-arn")
                                                                                      .streamStatus(StreamStatus.ACTIVE)
                                                                                      .build()).build());

        // Policy AWS request mocks
        when(iamClient.createRole(isA(Consumer.class)))
                .thenReturn(CreateRoleResponse.builder().role(Role.builder().build()).build());
        when(iamClient.putRolePolicy(isA(Consumer.class)))
                .thenReturn(PutRolePolicyResponse.builder().build());
        when(iamClient.getRole(isA(Consumer.class)))
                .thenReturn(GetRoleResponse.builder().role(Role.builder().arn("role-arn").build()).build());

        // Subscription AWS request mocks
        when(logsClient.putSubscriptionFilter(isA(PutSubscriptionFilterRequest.class)))
                .thenReturn(PutSubscriptionFilterResponse.builder().build());

    }

    @Test
    public void testAll() throws InterruptedException {

        // Stream
        final KinesisNewStreamRequest request =
                KinesisNewStreamRequest.create(REGION, KEY, SECRET, "stream-name");
        final KinesisNewStreamResponse streamResponse = setupResource.createNewKinesisStream(request);

        // Policy
        final CreateRolePermissionRequest policyRequest =
                CreateRolePermissionRequest.create(REGION, KEY, SECRET, streamResponse.streamName(),
                                                   streamResponse.streamArn(), "role-name", "role-policy-name");
        final CreateRolePermissionResponse policyResponse = setupResource.createPolicies(policyRequest);

        // Subscription
        final CreateLogSubscriptionRequest subscriptionRequest =
                CreateLogSubscriptionRequest.create(REGION, KEY, SECRET, "log-group-name", "filter-name",
                                                    "filter-pattern", streamResponse.streamArn(), policyResponse.roleArn());
        final CreateLogSubscriptionResponse subscriptionResponse = setupResource.createSubscription(subscriptionRequest);
    }
}
