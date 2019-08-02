package org.graylog.integrations.aws.resources.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import java.util.List;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisFullSetupResponse {

    private static final String SUCCESS = "success";
    private static final String EXPLANATION = "explanation";
    private static final String SETUP_STEPS = "setup_steps";

    @JsonProperty(SUCCESS)
    public abstract boolean success();

    @JsonProperty(EXPLANATION)
    public abstract String explanation();

    @JsonProperty(SETUP_STEPS)
    public abstract List<KinesisFullSetupResponseStep> setupSteps();

    public static KinesisFullSetupResponse create(@JsonProperty(SUCCESS) boolean success,
                                                  @JsonProperty(EXPLANATION) String explanation,
                                                  @JsonProperty(SETUP_STEPS) List<KinesisFullSetupResponseStep> setupSteps) {
        return new AutoValue_KinesisFullSetupResponse(success, explanation, setupSteps);
    }
}