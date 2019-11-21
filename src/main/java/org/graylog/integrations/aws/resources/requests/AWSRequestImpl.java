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

    @Nullable
    @JsonProperty(CLOUDWATCH_ENDPOINT)
    public abstract String cloudwatchEndpoint();

    @Nullable
    @JsonProperty(DYNAMODB_ENDPOINT)
    public abstract String dynamodbEndpoint();

    @Nullable
    @JsonProperty(IAM_ENDPOINT)
    public abstract String iamEndpoint();

    @Nullable
    @JsonProperty(KINESIS_ENDPOINT)
    public abstract String kinesisEndpoint();

    @JsonCreator
    public static AWSRequestImpl create(@JsonProperty(REGION) String region,
                                        @JsonProperty(AWS_ACCESS_KEY_ID) String awsAccessKeyId,
                                        @JsonProperty(AWS_SECRET_ACCESS_KEY) String awsSecretAccessKey,
                                        @JsonProperty(ASSUME_ROLE_ARN) String assumeRoleArn,
                                        @JsonProperty(CLOUDWATCH_ENDPOINT) String cloudwatchEndpoint,
                                        @JsonProperty(DYNAMODB_ENDPOINT) String dynamodbEndpoint,
                                        @JsonProperty(IAM_ENDPOINT) String iamEndpoint,
                                        @JsonProperty(KINESIS_ENDPOINT) String kinesisEndpoint) {
        return new AutoValue_AWSRequestImpl(region, awsAccessKeyId, awsSecretAccessKey, assumeRoleArn,
                                            cloudwatchEndpoint, dynamodbEndpoint, iamEndpoint, kinesisEndpoint);
    }
}