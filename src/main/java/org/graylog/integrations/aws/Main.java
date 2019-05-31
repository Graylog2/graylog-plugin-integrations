package org.graylog.integrations.aws;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        boolean fromStart = true;

        // CONFIGURATION
        String region = "us-east-1";

        // CLOUDWATCH
        CloudWatchLogsClient cloudWatchLogsClient = CloudWatchService.createCloudWatchLogClient(region);

        //Get all the logGroupName(s) available
        // TODO optimize this
        ArrayList<String> logGroupNameList = new CloudWatchService().getLogGroupNames(region);
        String logGroupName = "/var/log/messages";

        //Get all the logStreamName(s) available
        ArrayList<String> logStreamNameList = CloudWatchService.getStreamNameList(cloudWatchLogsClient, logGroupName);

        // PULL LOGS
        GetLogEventsRequest getLogEventsRequest = CloudWatchService.createGetLogEventRequest("/var/log/messages", "i-09c80ef1838f091e1", fromStart);

        FilterLogEventsRequest filterLogEventsRequest = FilterLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamNames(logStreamNameList)
                .interleaved(false) //false produces more logs
                .build();

    }
}
