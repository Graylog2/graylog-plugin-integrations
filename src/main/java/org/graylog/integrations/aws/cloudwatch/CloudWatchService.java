package org.graylog.integrations.aws.cloudwatch;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.DescribeLogGroupsIterable;

import javax.inject.Inject;
import java.util.ArrayList;

public class CloudWatchService {

    private CloudWatchLogsClientBuilder logsClientBuilder;

    @Inject
    public CloudWatchService(CloudWatchLogsClientBuilder logsClientBuilder) {
        this.logsClientBuilder = logsClientBuilder;
    }

    /**
     * Returns a list of log groups that exist in CloudWatch.
     *
     * @param region The AWS region
     * @return A list of log groups in alphabetical order.
     */
    public ArrayList<String> getLogGroupNames(String region) {

        final CloudWatchLogsClient cloudWatchLogsClient = logsClientBuilder.region(Region.of(region)).build();
        final DescribeLogGroupsRequest describeLogGroupsRequest = DescribeLogGroupsRequest.builder().build();
        final DescribeLogGroupsIterable responses = cloudWatchLogsClient.describeLogGroupsPaginator(describeLogGroupsRequest);

        final ArrayList<String> groupNameList = new ArrayList<>();
        for (DescribeLogGroupsResponse response : responses) {
            for (int c = 0; c < response.logGroups().size(); c++) {
                groupNameList.add(response.logGroups().get(c).logGroupName());
            }
        }
        return groupNameList;
    }
}