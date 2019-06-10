package org.graylog.integrations.aws;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;
import software.amazon.awssdk.services.kinesis.model.StartingPosition;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardRequest;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String kinesisStream = "integrations-test-kinesis-stream";
        KinesisClient kinesisClient = KinesisClient.builder().region(Region.US_EAST_1).build();

        // Add in arbitrary data
        byte[] bytes = "Around the world, Around the world.".getBytes();


        List<PutRecordsRequestEntry> putRecordsRequestEntryList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PutRecordsRequestEntry putRecordsRequestEntry = PutRecordsRequestEntry.builder()
                    .data(SdkBytes.fromByteArray(bytes))
                    .partitionKey(String.format("partitionKey-%d", i))
                    .build();
            putRecordsRequestEntryList.add(putRecordsRequestEntry);
        }
        PutRecordsRequest putRecordsRequest = PutRecordsRequest.builder().streamName(kinesisStream).records(putRecordsRequestEntryList).build();
        kinesisClient.putRecords(putRecordsRequest);
        System.out.println(putRecordsRequest);

        // Find shards that exist (there may be more than 1) and acquire shardId
        // Assume only one exists for now
        ListShardsRequest listShardsRequest = ListShardsRequest.builder().streamName(kinesisStream).build();

        GetShardIteratorRequest getShardIteratorRequest = GetShardIteratorRequest.builder()
                .streamName(kinesisStream)
                .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
                .build();

        ListShardsResponse listShardsResponse = kinesisClient.listShards(listShardsRequest);
        SubscribeToShardRequest subscribeToShardRequest = SubscribeToShardRequest.builder()
                .startingPosition(StartingPosition.builder().type(ShardIteratorType.TRIM_HORIZON).build())
                .build();

        System.out.print("listShardsRequest:  " + listShardsRequest);


        // Get the shardIterator and start with the oldest record in the stream
        //GetShardIteratorRequest getShardIteratorRequest = GetShardIteratorRequest.builder().shardIteratorType(ShardIteratorType.TRIM_HORIZON).build();
        // Create shardIterator to find shards
        // Call getRecord in a loop (use GetShardIterator)


    }
}
