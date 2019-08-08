package org.graylog.integrations.aws.service;

import org.graylog.integrations.aws.resources.requests.CreateLogSubscriptionRequest;
import org.graylog.integrations.aws.resources.responses.CreateLogSubscriptionResponse;
import org.graylog.integrations.aws.resources.responses.LogGroupsResponse;
import org.graylog2.shared.utilities.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.Distribution;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.DescribeLogGroupsIterable;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
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
     * @param awsAccessKeyId     The AWS accessKey
     * @param awsSecretAccessKey The AWS secretKey
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

        if (groupNameList.isEmpty()) {
            throw new BadRequestException(String.format("No CloudWatch log groups were found in the [%s] region.", region));
        }

        return LogGroupsResponse.create(groupNameList, groupNameList.size());
    }

    public CreateLogSubscriptionResponse addSubscriptionFilter(CreateLogSubscriptionRequest logSubscriptionRequest) {
        CloudWatchLogsClient cloudWatch = createClient(logSubscriptionRequest.region(),
                                                       logSubscriptionRequest.awsAccessKeyId(),
                                                       logSubscriptionRequest.awsSecretAccessKey());
        final PutSubscriptionFilterRequest putSubscriptionFilterRequest =
                PutSubscriptionFilterRequest.builder()
                                            .logGroupName(logSubscriptionRequest.logGroupName())
                                            .filterName(logSubscriptionRequest.filterName())
                                            .filterPattern(logSubscriptionRequest.filterPattern())
                                            .destinationArn(logSubscriptionRequest.destinationStreamArn())
                                            .roleArn(logSubscriptionRequest.getRoleArn())
                                            .distribution(Distribution.BY_LOG_STREAM)
                                            .build();
        try {
            cloudWatch.putSubscriptionFilter(putSubscriptionFilterRequest);
            String explanation = String.format("Success. The subscription filter [%s] was added for the CloudWatch log group [%s].",
                                 logSubscriptionRequest.filterName(), logSubscriptionRequest.logGroupName());
            return CreateLogSubscriptionResponse.create(explanation);
        } catch (Exception e) {
            final String specificError = ExceptionUtils.formatMessageCause(e);
            final String responseMessage = String.format("Attempt to add subscription [%s] to Cloudwatch log group " +
                                                         "[%s] failed due to the following exception: [%s]",
                                                         logSubscriptionRequest.filterName(),
                                                         logSubscriptionRequest.logGroupName(), specificError);
            LOG.error(responseMessage);
            throw new BadRequestException(responseMessage, e);
        }
    }
}