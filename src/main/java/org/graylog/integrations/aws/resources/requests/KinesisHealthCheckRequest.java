package org.graylog.integrations.aws.resources.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import javax.annotation.Nullable;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisHealthCheckRequest {

    @JsonProperty
    public abstract String region();

    @JsonProperty
    public abstract String streamName();

    // Optional. This will only be supplied for CloudWatch integrations, and will not be supplied for direct Kinesis.
    @JsonProperty
    @Nullable
    public abstract String logGroupName();

    @JsonProperty
    public abstract String awsAccessKeyId();

    @JsonProperty
    public abstract String awsSecretAccessKey();

    public static KinesisHealthCheckRequest create(@JsonProperty("region") String region,
                                                   @JsonProperty("stream_name") String streamName,
                                                   @JsonProperty("log_group_name") String logGroupName,
                                                   @JsonProperty("aws_access_key_id") String awsAccessKeyId,
                                                   @JsonProperty("aws_secret_access_key") String awsSecretAccessKey) {

        return new AutoValue_KinesisHealthCheckRequest(region, streamName, awsAccessKeyId, awsSecretAccessKey);
    }
}