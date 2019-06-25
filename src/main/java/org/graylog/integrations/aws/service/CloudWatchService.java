package org.graylog.integrations.aws.service;

import org.graylog.integrations.aws.resources.responses.LogGroupsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.DescribeLogGroupsIterable;

import javax.inject.Inject;
import java.util.ArrayList;

public class CloudWatchService {

    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchService.class);

    private CloudWatchLogsClientBuilder logsClientBuilder;

    @Inject
    public CloudWatchService(CloudWatchLogsClientBuilder logsClientBuilder) {
        this.logsClientBuilder = logsClientBuilder;
    }

    private CloudWatchLogsClient createClient(String regionName, String accessKeyId, String secretAccessKey) {

        return logsClientBuilder.region(Region.of(regionName))
                                .credentialsProvider(AWSService.buildCredentialProvider(accessKeyId, secretAccessKey))
                                .build();
    }

    /**
     * Returns a list of log groups that exist in CloudWatch.
     *
     * @param region             The AWS region
     * @param awsAccessKeyId
     * @param awsSecretAccessKey The AWS region
     * @return A list of log groups in alphabetical order.
     */
    public LogGroupsResponse getLogGroupNames(String region, String awsAccessKeyId, String awsSecretAccessKey) {

        final CloudWatchLogsClient cloudWatchLogsClient = createClient(region, awsAccessKeyId, awsSecretAccessKey);
        final DescribeLogGroupsRequest describeLogGroupsRequest = DescribeLogGroupsRequest.builder().build();
        final DescribeLogGroupsIterable responses = cloudWatchLogsClient.describeLogGroupsPaginator(describeLogGroupsRequest);

        final ArrayList<String> groupNameList = new ArrayList<>();
        for (DescribeLogGroupsResponse response : responses) {
            for (int c = 0; c < response.logGroups().size(); c++) {
                groupNameList.add(response.logGroups().get(c).logGroupName());
            }
        }
        LOG.debug("Log groups queried: [{}]", groupNameList);
        return LogGroupsResponse.create(groupNameList, groupNameList.size());
    }
}