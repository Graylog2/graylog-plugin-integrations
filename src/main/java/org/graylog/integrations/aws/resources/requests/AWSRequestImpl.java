package org.graylog.integrations.aws.resources.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import javax.annotation.Nullable;

/**
 * A common implementation on AWSRequest, which can be used for any AWS request that just needs region and credentials.
 */
@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class AWSRequestImpl implements AWSRequest {

    @JsonProperty(REGION)
    public abstract String region();

    @JsonProperty(AWS_ACCESS_KEY_ID)
    public abstract String awsAccessKeyId();

    @JsonProperty(AWS_SECRET_ACCESS_KEY)
    public abstract String awsSecretAccessKey();

    @Nullable
    @JsonProperty(ASSUME_ROLE_ARN)
    public abstract String assumeRoleArn();

    @JsonCreator
    public static AWSRequestImpl create(@JsonProperty(REGION) String region,
                                        @JsonProperty(AWS_ACCESS_KEY_ID) String awsAccessKeyId,
                                        @JsonProperty(AWS_SECRET_ACCESS_KEY) String awsSecretAccessKey,
                                        @JsonProperty(ASSUME_ROLE_ARN) String assumeRoleArn) {
        return new AutoValue_AWSRequestImpl(region, awsAccessKeyId, awsSecretAccessKey, assumeRoleArn);
    }
}