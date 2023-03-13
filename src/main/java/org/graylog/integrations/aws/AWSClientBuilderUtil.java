/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package org.graylog.integrations.aws;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.graylog.integrations.aws.resources.requests.AWSRequest;
import org.graylog2.security.encryption.EncryptedValue;
import org.graylog2.security.encryption.EncryptedValueService;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.client.builder.SdkClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.IamClientBuilder;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;

import javax.inject.Inject;
import java.net.URI;
import java.util.Optional;

/**
 * Responsible for initializing and building AWS SDK clients. This logic is centralized in one place to ensure consistency amongst the
 * clients and their initialization.
 */
public class AWSClientBuilderUtil {

    private final EncryptedValueService encryptedValueService;

    @Inject
    public AWSClientBuilderUtil(EncryptedValueService encryptedValueService) {
        this.encryptedValueService = encryptedValueService;
    }

    /**
     * Initialize the builder with the appropriate authorization, region, and endpoints.
     *
     * @param builder  Any AWS client builder.
     * @param endpoint See {@link SdkClientBuilder#endpointOverride(java.net.URI)} javadoc.
     * @param region   The region to specify on the client.
     */
    public static void initializeBuilder(AwsClientBuilder builder, String endpoint, Region region, AwsCredentialsProvider credentialsProvider) {
        builder.region(region);
        builder.credentialsProvider(credentialsProvider);

        // The endpoint override explicitly overrides the default URL used for all AWS API communication.
        if (StringUtils.isNotEmpty(endpoint)) {
            builder.endpointOverride(URI.create(endpoint));
        }
    }

    /**
     * Initialize and build the CloudWatch client.
     *
     * @param clientBuilder The builder, which was supplied through dependency injection.
     * @param request       The full AWSRequest.
     * @return A fully built {@link CloudWatchLogsClient}
     */
    public CloudWatchLogsClient buildClient(CloudWatchLogsClientBuilder clientBuilder, AWSRequest request) {
        Preconditions.checkNotNull(request.region(), "An AWS region is required.");
        AWSClientBuilderUtil.initializeBuilder(clientBuilder,
                request.cloudwatchEndpoint(),
                Region.of(request.region()),
                AWSAuthFactory.create(request.region(),
                        request.awsAccessKeyId(),
                        decryptSecretAccessKey(request.awsSecretAccessKey()),
                        request.assumeRoleArn()));

        return clientBuilder.build();
    }

    /**
     * Initialize and build the Kinesis client.
     *
     * @param clientBuilder The builder, which was supplied through dependency injection.
     * @param request       The full AWSRequest.
     * @return A fully built {@link KinesisClient}
     */
    public KinesisClient buildClient(KinesisClientBuilder clientBuilder, AWSRequest request) {

        AWSClientBuilderUtil.initializeBuilder(clientBuilder,
                request.kinesisEndpoint(),
                Region.of(request.region()),
                AWSAuthFactory.create(request.region(),
                        request.awsAccessKeyId(),
                        decryptSecretAccessKey(request.awsSecretAccessKey()),
                        request.assumeRoleArn()));

        return clientBuilder.build();
    }

    /**
     * Initialize and build the IAM client.
     *
     * @param clientBuilder The builder, which was supplied through dependency injection.
     * @param request       The full AWSRequest.
     * @return A fully built {@link IamClient}
     */
    public IamClient buildClient(IamClientBuilder clientBuilder, AWSRequest request) {
        Region iamRegion = Region.AWS_GLOBAL;
        if (request.region().contains("gov")) {
            iamRegion = Region.AWS_US_GOV_GLOBAL;
        }

        AWSClientBuilderUtil.initializeBuilder(clientBuilder,
                request.iamEndpoint(),
                iamRegion, // Always specify the appropriate global region for the IAM client.
                AWSAuthFactory.create(request.region(), // The AWSAuthProvider must still use the user-specified region, since a role might need to be assumed in that region.
                        request.awsAccessKeyId(),
                        decryptSecretAccessKey(request.awsSecretAccessKey()),
                        request.assumeRoleArn()));
        return clientBuilder.build();
    }

    private String decryptSecretAccessKey(EncryptedValue secretAccessKey) {
        return encryptedValueService.decrypt(Optional.ofNullable(secretAccessKey).orElse(EncryptedValue.createUnset()));
    }
}
