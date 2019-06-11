package org.graylog.integrations.aws;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String kinesisStream = "integrations-test-kinesis-stream";
        KinesisClient kinesisClient = KinesisClient.builder().region(Region.US_EAST_1).build();

        // Add in arbitrary records
        byte[] bytes = "Around the world, Around the world.".getBytes();

        //ADD RECORDS IN
        // Create an arbitrary collection of records
        List<PutRecordsRequestEntry> putRecordsRequestEntryList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PutRecordsRequestEntry putRecordsRequestEntry = PutRecordsRequestEntry.builder()
                    .data(SdkBytes.fromByteArray(bytes))
                    .partitionKey(String.format("partitionKey-%d", i))
                    .build();
            putRecordsRequestEntryList.add(putRecordsRequestEntry);
        }
        // Create a PutRecordRequest and pass to the Kinesis client
        PutRecordsRequest putRecordsRequest = PutRecordsRequest.builder()
                .streamName(kinesisStream)
                .records(putRecordsRequestEntryList)
                .build();
        // Kinesis client adds collection to kinesis stream
        kinesisClient.putRecords(putRecordsRequest);

        //KinesisService.retrieveKinesisLogs(kinesisStream);

        // TAKE RECORDS OUT
        // Create ListShard request and response and designate the Kinesis stream
        ListShardsRequest listShardsRequest = ListShardsRequest.builder().streamName(kinesisStream).build();
        ListShardsResponse listShardsResponse = kinesisClient.listShards(listShardsRequest);

        String shardID = "";
        int shardNum = listShardsResponse.shards().size();

        // Iterate through the shards that exist
        String nextShardIterator = "";
        for (int i = 0; i < shardNum; i++) {
            shardID = listShardsResponse.shards().get(i).shardId();
            System.out.println("Shard Id: " + shardID);
            GetShardIteratorRequest getShardIteratorRequest = GetShardIteratorRequest.builder()
                    .shardId(shardID)
                    .streamName(kinesisStream)
                    .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
                    .build();
            String shardIterator = kinesisClient.getShardIterator(getShardIteratorRequest).shardIterator();

            System.out.println("Shard Interator: " + shardIterator);
            GetRecordsRequest getRecordsRequest = GetRecordsRequest.builder().shardIterator(shardIterator).build();
            GetRecordsResponse getRecordsResponse = kinesisClient.getRecords(getRecordsRequest);
            // TODO iterate through all the shardIterators to account for all the records
//            nextShardIterator = getRecordsResponse.nextShardIterator();
//            GetRecordsRequest getRecordsRequest = GetRecordsRequest.builder().shardIterator(nextShardIterator).build();
//            GetRecordsResponse getRecordsResponse = kinesisClient.getRecords(getRecordsRequest);
//            System.out.println(getRecordsResponse.records().size());
//            nextShardIterator = getRecordsResponse.nextShardIterator();

            int recordSize = getRecordsResponse.records().size();
            System.out.println("Record size: " + recordSize);
            for (int j = 0; j < recordSize; j++) {
                System.out.println("[" + j + "]" + new String(getRecordsResponse.records().get(j).data().asByteArray()));
            }
        }
    }
}
