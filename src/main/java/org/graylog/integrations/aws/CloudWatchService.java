package org.graylog.integrations.aws;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CloudWatchService {

    private CloudWatchLogsClient logsClient;

    @Inject
    public CloudWatchService() {

    }

    static CloudWatchLogsClient createCloudWatchLogClient(String region) {

        CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder()
                //.credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .region(Region.of(region))
                .build();
        return logsClient;
    }

    public CloudWatchLogsClient createGetLogRequest(String logGroupName, String logStreamName, boolean fromStart) {
        logsClient.getLogEvents(createGetLogEventRequest(logGroupName, logStreamName, fromStart));
        return logsClient;
    }

    static GetLogEventsRequest createGetLogEventRequest(String logGroupName, String logStreamName, boolean fromStart) {
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

    public ArrayList<String> getLogGroupNames(String region) {

        ArrayList<String> logGroupNames = new ArrayList<>();
        // TODO optimize this
        Iterator<DescribeLogGroupsResponse> logGroupsIterator = CloudWatchService.createCloudWatchLogClient(region).describeLogGroupsPaginator().iterator();
        DescribeLogGroupsResponse response = logGroupsIterator.next();
        for (int c = 0; c < response.logGroups().size(); c++) {
            response.logGroups().get(c).logGroupName();
            logGroupNames.add(response.logGroups().get(c).logGroupName());
        }
        return logGroupNames;
    }

    static ArrayList<String> getStreamNameList(CloudWatchLogsClient cloudWatchLogsClient, String logGroupName) {
        DescribeLogStreamsRequest logStreamsRequest = DescribeLogStreamsRequest.builder()
                .logGroupName(logGroupName)
                .build();
        int logStreamListSize = cloudWatchLogsClient.describeLogStreams(((logStreamsRequest))).logStreams().size();
        ArrayList<String> streamNameList = new ArrayList<>();
        for (int c = 0; c < logStreamListSize; c++) {
            String logStreamName = cloudWatchLogsClient.describeLogStreams(((logStreamsRequest))).logStreams().get(c).logStreamName();
            streamNameList.add(logStreamName);
        }
        return streamNameList;
    }

    List<String> fakeLogGroups() {

        ArrayList<String> logGroups = new ArrayList<>();
        logGroups.add("test-group1");
        logGroups.add("test-group2");
        return logGroups;
    }
}
