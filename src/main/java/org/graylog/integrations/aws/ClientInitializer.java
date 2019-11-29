package org.graylog.integrations.aws;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.core.client.builder.SdkClientBuilder;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

public class ClientInitializer {

    // Non-instantiable Util class.
    private ClientInitializer() {

    }

    /**
     * Initialize the builder with the appropriate authorization, region, and endpoints.
     * @param builder Any AWS client builder.
     * @param endpoint See {@link SdkClientBuilder#endpointOverride(java.net.URI)} javadoc.
     * @param region
     */
    public static void initializeBuilder(AwsClientBuilder builder, String endpoint, Region region, AwsCredentialsProvider credentialsProvider) {
        builder.region(region);
        builder.credentialsProvider(credentialsProvider);

        // The endpoint override explicitly overrides the default URL used for all
        // AWS API communication.
        if (StringUtils.isNotEmpty(endpoint)) {
            builder.endpointOverride(URI.create(endpoint));
        }
    }
}