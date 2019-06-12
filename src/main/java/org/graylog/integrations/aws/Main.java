package org.graylog.integrations.aws;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String kinesisStream = "integrations-test-kinesis-stream";
        KinesisClient kinesisClient = KinesisClient.builder().region(Region.US_EAST_1).build();

        addRecords(kinesisStream, kinesisClient);
        KinesisService.retrieveKinesisLogs(kinesisStream, kinesisClient);
    }

    private static void addRecords(String kinesisStream, KinesisClient kinesisClient) {

        // Add in arbitrary records
        byte[] bytes = "Around the world, Around the world.".getBytes();

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
    }
}
