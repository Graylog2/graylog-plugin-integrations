package org.graylog.integrations.aws.service;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.graylog.integrations.aws.resources.responses.AvailableAWSService;
import org.graylog.integrations.aws.resources.responses.AvailableAWSServiceSummmary;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.RegionMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for all AWS CloudWatch business logic and SDK usages.
 */
public class AWSService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);

    /**
     * @return A list of all available regions.
     */
    public List<RegionResponse> getAvailableRegions() {

        return Region.regions().stream()
                     // Ignore the global region. CloudWatch and Kinesis cannot be used with global regions.
                     .filter(r -> !r.isGlobalRegion())
                     .map(r -> {
                         // Build a single AWSRegionResponse with id, description, and displayValue.
                         RegionMetadata regionMetadata = r.metadata();
                         String displayValue = String.format("%s: %s", regionMetadata.description(), regionMetadata.id());
                         return RegionResponse.create(regionMetadata.id(), regionMetadata.description(), displayValue);
                     }).collect(Collectors.toList());
    }

    /**
     * Checks that the supplied accessKey and secretKey are not null or blank
     *
     * @return A credential provider
     */
    static StaticCredentialsProvider buildCredentialProvider(String accessKeyId, String secretAccessKey) {
        Preconditions.checkArgument(StringUtils.isNotBlank(accessKeyId), "An AWS access key is required.");
        Preconditions.checkArgument(StringUtils.isNotBlank(secretAccessKey), "An AWS secret key is required.");

        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
    }

    /**
     * @return A list of available AWS services supported by the AWS Graylog AWS integration.
     */
    public AvailableAWSServiceSummmary getAvailableServices() {

        ArrayList<AvailableAWSService> services = new ArrayList<>();
        AvailableAWSService cloudWatchService =
                AvailableAWSService.create("CloudWatch",
                                           "Retrieve CloudWatch logs via Kinesis. Kinesis allows streaming of the logs " +
                                                   "in real time. AWS CloudWatch is a monitoring and management service built " +
                                                   "for developers, system operators, site reliability engineers (SRE), " +
                                                   "and IT managers.",
                                           "{\n" +
                                                   "  \"Version\": \"2019-06-19\",\n" +
                                                   "  \"Statement\": [\n" +
                                                   "    {\n" +
                                                   "      \"Sid\": \"GraylogCloudWatchPolicy\",\n" +
                                                   "      \"Effect\": \"Allow\",\n" +
                                                   "      \"Action\": [\n" +
                                                   "        \"cloudwatch:PutMetricData\",\n" +
                                                   "        \"dynamodb:CreateTable\",\n" +
                                                   "        \"dynamodb:DescribeTable\",\n" +
                                                   "        \"dynamodb:GetItem\",\n" +
                                                   "        \"dynamodb:PutItem\",\n" +
                                                   "        \"dynamodb:Scan\",\n" +
                                                   "        \"dynamodb:UpdateItem\",\n" +
                                                   "        \"ec2:DescribeInstances\",\n" +
                                                   "        \"ec2:DescribeNetworkInterfaceAttribute\",\n" +
                                                   "        \"ec2:DescribeNetworkInterfaces\",\n" +
                                                   "        \"elasticloadbalancing:DescribeLoadBalancerAttributes\",\n" +
                                                   "        \"elasticloadbalancing:DescribeLoadBalancers\",\n" +
                                                   "        \"kinesis:GetRecords\",\n" +
                                                   "        \"kinesis:GetShardIterator\",\n" +
                                                   "        \"kinesis:ListShards\"\n" +
                                                   "      ],\n" +
                                                   "      \"Resource\": \"*\"\n" +
                                                   "    }\n" +
                                                   "  ]\n" +
                                                   "}",
                                           "Requires Kinesis",
                                           "https://aws.amazon.com/cloudwatch/"
                );
        services.add(cloudWatchService);
        return AvailableAWSServiceSummmary.create(services, services.size());
    }
}