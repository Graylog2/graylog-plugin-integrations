package org.graylog.integrations;


import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;

public class CrossAccountRoleTest {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Please supply the ARN to assume and the stream name to create as the first two command line arguments.");
            return;
        }

        System.out.println("Assuming Role ARN: " + args[0] + "");

        final DefaultCredentialsProvider defaultCredentialsProvider = DefaultCredentialsProvider.create();
        StsClient stsClient = StsClient.builder().region(Region.US_EAST_1).credentialsProvider(defaultCredentialsProvider).build();
        String roleSessionName = String.format("ACCOUNT_%s",
                                               stsClient.getCallerIdentity(GetCallerIdentityRequest.builder().build()).account());

        // The custom role session name is extra metadata, which will be logged in AWS CloudTrail with each request
        // to help with debugging.
        final StsAssumeRoleCredentialsProvider stsProvider = StsAssumeRoleCredentialsProvider.builder().refreshRequest(AssumeRoleRequest.builder()
                                                                                                                                        .roleSessionName(roleSessionName)
                                                                                                                                        .roleArn(args[0])
                                                                                                                                        .build())
                                                                                             .stsClient(stsClient)
                                                                                             .build();

        final KinesisClientBuilder clientBuilder = KinesisClient.builder();
        clientBuilder.credentialsProvider(stsProvider);
        clientBuilder.region(Region.US_EAST_1);

        final KinesisClient client = clientBuilder.build();
        client.createStream(CreateStreamRequest.builder().streamName(args[1]).shardCount(1).build());
    }
}
