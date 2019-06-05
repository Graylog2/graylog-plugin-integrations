package org.graylog.integrations.aws.resources.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisHealthCheckRequest {

    @JsonProperty
    public abstract String region();

    @JsonProperty
    public abstract String streamName();

    @JsonProperty
    public abstract String awsAccessKeyId();

    @JsonProperty
    public abstract String awsSecretAccessKey();

    public static KinesisHealthCheckRequest create(@JsonProperty("region") String region,
                                                   @JsonProperty("stream_name") String streamName,
                                                   @JsonProperty("aws_access_key_id") String awsAccessKeyId,
                                                   @JsonProperty("aws_secret_access_key") String awsSecretAccessKey) {
        return new AutoValue_KinesisHealthCheckRequest(region, streamName, awsAccessKeyId, awsSecretAccessKey);
    }
}