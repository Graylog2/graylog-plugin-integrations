package org.graylog.integrations.aws.resources.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import java.util.List;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisFullSetupResponseStep {

    private static final String SUCCESS = "success";
    private static final String STEP_NAME = "step_name";
    private static final String STEP_DESCRIPTION = "step_description";

    @JsonProperty(SUCCESS)
    public abstract boolean success();

    @JsonProperty(STEP_NAME)
    public abstract String stepName();

    @JsonProperty(STEP_DESCRIPTION)
    public abstract String stepDescription();

    public static KinesisFullSetupResponseStep create(@JsonProperty(SUCCESS) boolean success,
                                                      @JsonProperty(STEP_NAME) String stepName,
                                                      @JsonProperty(STEP_DESCRIPTION) String stepDescription) {
        return new AutoValue_KinesisFullSetupResponseStep(success, stepName, stepDescription);
    }
}