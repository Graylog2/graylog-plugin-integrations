package org.graylog.integrations.aws.resources.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class CreateRolePermissionRequest implements AWSRequest {

    private static final String STREAM_NAME = "stream_name";
    private static final String ROLE_NAME = "role_name";
    private static final String ROLE_POLICY_NAME = "role_policy_name";

    @JsonProperty(REGION)
    public abstract String region();

    @JsonProperty(AWS_ACCESS_KEY_ID)
    public abstract String awsAccessKeyId();

    @JsonProperty(AWS_SECRET_ACCESS_KEY)
    public abstract String awsSecretAccessKey();

    @JsonProperty(STREAM_NAME)
    public abstract String streamName();

    @JsonProperty(ROLE_NAME)
    public abstract String roleName();

    @JsonProperty(ROLE_POLICY_NAME)
    public abstract String rolePolicyName();

    @JsonCreator
    public static CreateRolePermissionRequest create(@JsonProperty(REGION) String region,
                                                     @JsonProperty(AWS_ACCESS_KEY_ID) String awsAccessKeyId,
                                                     @JsonProperty(AWS_SECRET_ACCESS_KEY) String awsSecretAccessKey,
                                                     @JsonProperty(STREAM_NAME) String streamName,
                                                     @JsonProperty(ROLE_NAME) String roleName,
                                                     @JsonProperty(ROLE_POLICY_NAME) String rolePolicyName) {
        return new AutoValue_CreateRolePermissionRequest(region, awsAccessKeyId, awsSecretAccessKey, streamName, roleName, rolePolicyName);
    }
}
