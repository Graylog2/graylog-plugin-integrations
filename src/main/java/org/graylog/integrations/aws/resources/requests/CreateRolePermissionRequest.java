package org.graylog.integrations.aws.resources.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import javax.annotation.Nullable;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class CreateRolePermissionRequest implements AWSRequest {

    private static final String STREAM_NAME = "stream_name";
    private static final String STREAM_ARN = "stream_arn";

    @JsonProperty(REGION)
    public abstract String region();

    @JsonProperty(AWS_ACCESS_KEY_ID)
    public abstract String awsAccessKeyId();

    @JsonProperty(AWS_SECRET_ACCESS_KEY)
    public abstract String awsSecretAccessKey();

    @Nullable
    @JsonProperty(ASSUME_ROLE_ARN)
    public abstract String assumeRoleArn();

    @JsonProperty(STREAM_NAME)
    public abstract String streamName();

    @JsonProperty(STREAM_ARN)
    public abstract String streamArn();

    @JsonCreator
    public static CreateRolePermissionRequest create(@JsonProperty(REGION) String region,
                                                     @JsonProperty(AWS_ACCESS_KEY_ID) String awsAccessKeyId,
                                                     @JsonProperty(AWS_SECRET_ACCESS_KEY) String awsSecretAccessKey,
                                                     @JsonProperty(ASSUME_ROLE_ARN) String assumeRoleArn,
                                                     @JsonProperty(STREAM_NAME) String streamName,
                                                     @JsonProperty(STREAM_ARN) String streamArn) {
        return new AutoValue_CreateRolePermissionRequest(region, awsAccessKeyId, awsSecretAccessKey, assumeRoleArn, streamName, streamArn);
    }
}
