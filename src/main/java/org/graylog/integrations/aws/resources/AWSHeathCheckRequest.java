package org.graylog.integrations.aws.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class AWSHeathCheckRequest {

    @JsonProperty
    public abstract String region();

    @JsonProperty
    public abstract String logGroupName();

    @JsonProperty
    public abstract String awsAccessKeyId();

    @JsonProperty
    public abstract String awsSecretAccessKey();

    public static AWSHeathCheckRequest create(@JsonProperty("region") String region,
                                              @JsonProperty("log_group_name") String logGroupName,
                                              @JsonProperty("aws_access_key_id") String awsAccessKeyId,
                                              @JsonProperty("aws_secret_access_key") String awsSecretAccessKey) {
        return new AutoValue_AWSHeathCheckRequest(region, logGroupName, awsAccessKeyId, awsSecretAccessKey);
    }
}