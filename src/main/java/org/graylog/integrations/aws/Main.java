package org.graylog.integrations.aws;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;

import static org.graylog.integrations.aws.CloudWatchService.createCloudWatchLogClient;

public class Main {
    public static void main(String[] args) {

        String logGroupName;
        String logStreamName;
        int logLimit;
        int logGroupListSize;
        int logStreamListSize;

        // CONFIGURATION
        // Set credentials
        AwsBasicCredentials basicCredentials =
                AwsBasicCredentials.create(UserCredentials.accessKey, UserCredentials.secretKey);
        // Set Region
        Region region = Region.US_EAST_1;


        // CLOUDWATCH
        CloudWatchLogsClient cloudWatchLogsClient = createCloudWatchLogClient(basicCredentials, region);

        // Number of logGroupNames that exist
        logGroupListSize = cloudWatchLogsClient.describeLogGroups().logGroups().size();

        //List all the logGroupName(s) available
        for (int i = 0; i < logGroupListSize; i++) {
            logGroupName = cloudWatchLogsClient.describeLogGroups().logGroups().get(i).logGroupName();
            DescribeLogStreamsRequest logStreamsRequest = DescribeLogStreamsRequest.builder()
                    .logGroupName(logGroupName)
                    .build();
            CloudWatchService.printGroupNames(cloudWatchLogsClient);

            // Number of logStreamNames that exist
            logStreamListSize = cloudWatchLogsClient.describeLogStreams(((logStreamsRequest))).logStreams().size();
            System.out.print("Log Group Name: " + logGroupName + "\n");

            // List all logStream(s) available
            for (int j = 0; j < logStreamListSize; j++) {
                logStreamName = cloudWatchLogsClient.describeLogStreams(((logStreamsRequest))).logStreams().get(j).logStreamName();
                System.out.print("     Log Stream Name: " + logStreamName + "\n");
            }
        }

        // Hardcoded variables for test purposes
        logGroupName = "graylogs";
        logStreamName = "graylog";

        // Create GetLogEventRequest object
        GetLogEventsRequest getLogEventsRequest = GetLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName) // logStreamName is required
                //.startTime()
                //.endTime()
                //.nextToken()
                //.limit(logLimit)
                .startFromHead(true)
                .build();

        logLimit = cloudWatchLogsClient.getLogEvents(getLogEventsRequest).events().size();

        // Designate getLogEventsRequest from the START
        cloudWatchLogsClient.getLogEvents(getLogEventsRequest);
        System.out.print("\n Pull log from the START, and print to console.\n\n");

        //Iterate through all the events
        for (int i = 0; i < logLimit; i++) {
            System.out.print(" [" + i + "] " + cloudWatchLogsClient.getLogEvents(getLogEventsRequest).events().get(i) + "\n");
        }

        // Set next token
        String nextToken = cloudWatchLogsClient.getLogEvents(getLogEventsRequest).nextForwardToken();


        // TODO resolve nextToken issue
        // Designate getLogEventsRequest from LAST REQUEST
        //cloudWatchLogsClient.getLogEvents(logEventObject02);


    }
}
