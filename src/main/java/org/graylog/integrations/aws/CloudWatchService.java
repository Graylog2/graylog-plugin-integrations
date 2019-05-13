package org.graylog.integrations.aws;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;

public class CloudWatchService {

    public CloudWatchLogsClient logsClient;

    // Create a CloudwatchLog client
    public static CloudWatchLogsClient createCloudWatchLogClient(AwsCredentials basicCredentials, Region region) {

        CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .region(region)
                .build();

        return logsClient;
    }

    public CloudWatchLogsClient createGetLogRequest(String logGroupName, String logStreamName, boolean fromStart) {
        logsClient.getLogEvents(createGetLogEventRequest(logGroupName, logStreamName, fromStart));
        return logsClient;

    }

    public GetLogEventsRequest createGetLogEventRequest(String logGroupName, String logStreamName, boolean fromStart) {
        GetLogEventsRequest getLogEventsRequest = GetLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                //.startTime()
                //.endTime()
                //.nextToken(nextToken)
                //.limit(logLimit)
                .startFromHead(fromStart)
                .build();

        return getLogEventsRequest;
    }

    public static void printGroupNames(CloudWatchLogsClient cloudWatchLogsClient) {
        int logGroupListSize = cloudWatchLogsClient.describeLogGroups().logGroups().size();
        for (int i = 0; i < logGroupListSize; i++) {
            String logGroupName = cloudWatchLogsClient.describeLogGroups().logGroups().get(i).logGroupName();
            System.out.print("Log Group Name: " + logGroupName + "\n");
        }
    }


}
