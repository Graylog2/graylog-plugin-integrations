package org.graylog.integrations.aws.resources.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class CreateRolePermissionResponse {
    private static final String EXPLANATION = "explanation";
    private static final String ROLE_ARN = "role_arn";

    @JsonProperty(EXPLANATION)
    public abstract String explanation();

    @JsonProperty(ROLE_ARN)
    public abstract String roleArn();

    public static CreateRolePermissionResponse create(@JsonProperty(EXPLANATION) String explanation,
                                                      @JsonProperty(ROLE_ARN) String roleArn) {
        return new AutoValue_CreateRolePermissionResponse(explanation, roleArn);
    }
}
