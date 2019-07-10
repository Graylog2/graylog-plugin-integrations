package org.graylog.integrations.aws.resources.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisNewStreamRequest implements AWSRequest {

    private static final String STREAM_NAME = "stream_name";
    private static final String SHARD_COUNT = "shard_count";

    @JsonProperty(REGION)

    public abstract String region();

    @JsonProperty(AWS_ACCESS_KEY_ID)
    public abstract String awsAccessKeyId();

    @JsonProperty(AWS_SECRET_ACCESS_KEY)
    public abstract String awsSecretAccessKey();

    @JsonProperty(STREAM_NAME)
    public abstract String streamName();

    @JsonProperty(SHARD_COUNT)
    public abstract int shardCount();

    @JsonCreator
    public static KinesisNewStreamRequest create(@JsonProperty(REGION) String region,
                                                 @JsonProperty(AWS_ACCESS_KEY_ID) String awsAccessKeyId,
                                                 @JsonProperty(AWS_SECRET_ACCESS_KEY) String awsSecretAccessKey,
                                                 @JsonProperty(STREAM_NAME) String streamName,
                                                 @JsonProperty(SHARD_COUNT) int shardCount) {
        return new AutoValue_KinesisNewStreamRequest(region, awsAccessKeyId, awsSecretAccessKey, streamName, shardCount);
    }
}