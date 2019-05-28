package org.graylog.integrations.aws;

import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;

public class KinesisService {

    public static KinesisClient getKinesisClient() {
        KinesisClient kinesisClient =  KinesisClient.create();
        return kinesisClient;
    }

}
