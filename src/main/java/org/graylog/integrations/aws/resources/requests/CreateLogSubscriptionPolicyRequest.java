package org.graylog.integrations.aws.resources.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class CreateLogSubscriptionPolicyRequest implements AWSRequest {

    private static final String STREAM_ARN = "stream_arn";

    @JsonProperty(REGION)
    public abstract String region();

    @JsonProperty(AWS_ACCESS_KEY_ID)
    public abstract String awsAccessKeyId();

    @JsonProperty(AWS_SECRET_ACCESS_KEY)
    public abstract String awsSecretAccessKey();

    @JsonProperty(STREAM_ARN)
    public abstract String streamArn();

    @JsonCreator
    public static CreateLogSubscriptionPolicyRequest create(@JsonProperty(REGION) String region,
                                                            @JsonProperty(AWS_ACCESS_KEY_ID) String awsAccessKeyId,
                                                            @JsonProperty(AWS_SECRET_ACCESS_KEY) String awsSecretAccessKey,
                                                            @JsonProperty(STREAM_ARN) String streamArn) {
        return new AutoValue_CreateLogSubscriptionPolicyRequest(region, awsAccessKeyId, awsSecretAccessKey, streamArn);
    }
}