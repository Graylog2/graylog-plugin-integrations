package org.graylog.integrations.aws.service;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import com.google.common.base.Preconditions;
import org.graylog.integrations.aws.resources.responses.AvailableAWSService;
import org.graylog.integrations.aws.resources.responses.AvailableAWSServiceSummmary;
import org.apache.commons.lang.StringUtils;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.RegionMetadata;

import java.util.ArrayList;
import java.util.ArrayList;
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

    /**
     * @return A list of available AWS services supported by the AWS Graylog AWS integration.
     */
    public AvailableAWSServiceSummmary getAvailableServices() {

        ArrayList<AvailableAWSService> services = new ArrayList<>();
        AvailableAWSService cloudWatchService =
                AvailableAWSService.create("CloudWatch",
                                           "Retrieve CloudWatch logs via Kinesis. Kinesis allows streaming of the logs" +
                                           "in real time. Amazon CloudWatch is a monitoring and management service built" +
                                           "for developers, system operators, site reliability engineers (SRE), " +
                                           "and IT managers.",
                                           "{\n" +
                                           "  \"Version\": \"2012-10-17\",\n" +
                                           "  \"Statement\": [\n" +
                                           "    {\n" +
                                           "      \"Sid\": \"VisualEditor0\",\n" +
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