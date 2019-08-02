package org.graylog.integrations.aws.resources.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class CreateLogSubscriptionPolicyResponse {

    private static final String POLICY_NAME = "policy_name";
    private static final String POLICY_ARN = "policy_arn";

    @JsonProperty(POLICY_NAME)
    public abstract String policyName();

    @JsonProperty(POLICY_ARN)
    public abstract String policyArn();

    public static CreateLogSubscriptionPolicyResponse create(@JsonProperty(POLICY_NAME) String policyName,
                                                             @JsonProperty(POLICY_ARN) String policyArn) {
        return new AutoValue_CreateLogSubscriptionPolicyResponse(policyName, policyArn);
    }
}