package org.graylog.integrations.aws;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;

import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static void main(String[] args) {

        int logLimit;
        boolean fromStart = true;

        // CONFIGURATION
        AwsBasicCredentials awsUserCredentials = AWSConfigSettings.createUser();
        AWSConfigSettings.setRegion();

        // CLOUDWATCH
        CloudWatchLogsClient cloudWatchLogsClient = CloudWatchService.createCloudWatchLogClient(awsUserCredentials, Region.US_EAST_1);

        //List all the logGroupName(s) available
        ArrayList<String> logGroupNameList = CloudWatchService.getGroupNameList(cloudWatchLogsClient);
        System.out.println("Log Group Names Available:" + logGroupNameList);
        String logGroupName = "/var/log/messages";

        //List all the logStreamName(s) available
        ArrayList<String> logStreamNameList = CloudWatchService.getStreamNameList(cloudWatchLogsClient, logGroupName);
        System.out.println("Stream Names from " + logGroupName + ": " + logStreamNameList);

        // Pick a random logStreamName
        Random rand = new Random();
        int rng = rand.ints(0, (logStreamNameList.size() -1)).findFirst().getAsInt();

        // PULL LOGS
        // Create GetLogEventRequest object
        GetLogEventsRequest getLogEventsRequest = CloudWatchService.createGetLogEventRequest("/var/log/messages", "i-09c80ef1838f091e1",fromStart);

        logLimit = cloudWatchLogsClient.getLogEvents(getLogEventsRequest).events().size();

        // Designate getLogEventsRequest from the START
        //cloudWatchLogsClient.getLogEvents(getLogEventsRequest);
        FilterLogEventsRequest filterLogEventsRequest = FilterLogEventsRequest.builder().logGroupName(logGroupName).build();
        cloudWatchLogsClient.filterLogEvents(FilterLogEventsRequest.builder().build());


        //Iterate through all the events
        for (int i = 0; i < logLimit; i++) {
            if (i== logLimit-1) {
                System.out.println("[" + i + "] " + cloudWatchLogsClient.getLogEvents(getLogEventsRequest).events().get(i));
            }
        }

        // Set next token
        String nextToken = cloudWatchLogsClient.getLogEvents(getLogEventsRequest).nextForwardToken();


        // TODO resolve nextToken issue
        // Designate getLogEventsRequest from LAST REQUEST
        //cloudWatchLogsClient.getLogEvents(logEventObject02);


    }
}
