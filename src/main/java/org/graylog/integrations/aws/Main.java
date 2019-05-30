package org.graylog.integrations.aws;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardRequest;

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
                //logStreamNames(String... var1);
                //startTime(Long var1);
                //endTime(Long var1);
                //filterPattern(String var1);
                //nextToken(String var1);
                //limit(Integer var1);
                .interleaved(false) //false produces more logs
                .build();

//        cloudWatchLogsClient.filterLogEvents(filterLogEventsRequest);
//        cloudWatchLogsClient.getLogEventsPaginator(getLogEventsRequest).iterator();

        logGroupNameList.iterator();

        // Set next token
        String nextToken = cloudWatchLogsClient.getLogEvents(getLogEventsRequest).nextForwardToken();

        KinesisClient kinesisClient = KinesisService.getKinesisClient();
        kinesisClient.describeLimits();
        SubscribeToShardRequest subscribeToShardRequest = SubscribeToShardRequest.builder().shardId("shardID").build();
        GetRecordsRequest getRecordsRequest = GetRecordsRequest.builder().shardIterator("somethingGoesHere?").build();

        String shardIterator;
//        GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest(); //GetShardIteratorRequest();
//        getShardIteratorRequest.setStreamName(myStreamName);
//        getShardIteratorRequest.setShardId(shard.getShardId());
//        getShardIteratorRequest.setShardIteratorType("TRIM_HORIZON");

        //GetShardIteratorResult getShardIteratorResult = client.getShardIterator(getShardIteratorRequest);
//        shardIterator = getShardIteratorResult.getShardIterator();

        // TODO resolve nextToken issue
        // Designate getLogEventsRequest from LAST REQUEST
        //cloudWatchLogsClient.getLogEvents(logEventObject02);


    }
}
