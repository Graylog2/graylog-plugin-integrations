package org.graylog.integrations.aws.service;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.RegionMetadata;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for all AWS CloudWatch business logic.
 * <p>
 * This layer should not directly use the AWS SDK. All SDK operations should be performed in AWSClient.
 */
public class AWSService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);

    /**
     * @return A list of all available regions.
     */
    public List<RegionResponse> getAvailableRegions() {

        // This stream operation is just a way to convert a list of regions to the RegionResponse object.
        return Region.regions().stream()
                .filter(r -> !r.isGlobalRegion()) // Ignore the global region. We don't need it.
                .map(r -> {
                    // Build a single AWSRegionResponse with id, description, and displayValue.
                    RegionMetadata regionMetadata = r.metadata();
                    String displayValue = String.format("%s: %s", regionMetadata.description(), regionMetadata.id());
                    return RegionResponse.create(regionMetadata.id(), regionMetadata.description(), displayValue);
                }).collect(Collectors.toList());
    }

    //TODO Add getAWSServices List
    //List that contains all the supported AWS services (i.e. Cloudwatch, Kinesis)

    public static StaticCredentialsProvider validateCredentials(String accessKeyId, String secretAccessKey) {
        // Checking credentials are valid
        Preconditions.checkArgument(StringUtils.isNotBlank(accessKeyId), "An AWS access key is required.");
        Preconditions.checkArgument(StringUtils.isNotBlank(secretAccessKey), "An AWS secret key is required.");

        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
    }
}