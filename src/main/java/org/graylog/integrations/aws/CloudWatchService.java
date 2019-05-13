package org.graylog.integrations.aws;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;

import java.util.ArrayList;

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

    public static GetLogEventsRequest createGetLogEventRequest(String logGroupName, String logStreamName, boolean fromStart) {
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

    public static ArrayList<String> getGroupNameList(CloudWatchLogsClient cloudWatchLogsClient) {
        int logGroupListSize = cloudWatchLogsClient.describeLogGroups().logGroups().size();
        ArrayList<String> groupNameList = new ArrayList<>();
        for (int i = 0; i < logGroupListSize; i++) {
            String logGroupName = cloudWatchLogsClient.describeLogGroups().logGroups().get(i).logGroupName();
            groupNameList.add(logGroupName);
        }
        return groupNameList;
    }

    public static ArrayList<String> getStreamNameList(CloudWatchLogsClient cloudWatchLogsClient, String logGroupName) {
        DescribeLogStreamsRequest logStreamsRequest = DescribeLogStreamsRequest.builder()
                .logGroupName(logGroupName)
                .build();

        // Number of logStreamNames that exist
        int logStreamListSize = cloudWatchLogsClient.describeLogStreams(((logStreamsRequest))).logStreams().size();
        ArrayList<String> streamNameList = new ArrayList<>();
        for (int j = 0; j < logStreamListSize; j++) {
            String logStreamName = cloudWatchLogsClient.describeLogStreams(((logStreamsRequest))).logStreams().get(j).logStreamName();
            streamNameList.add(logStreamName);
        }
        return streamNameList;
    }

}
