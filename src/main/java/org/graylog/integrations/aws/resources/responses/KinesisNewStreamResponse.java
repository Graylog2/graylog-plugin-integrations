package org.graylog.integrations.aws.resources.responses;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisNewStreamResponse {

    private static final String EXPLANATION = "explanation";

    // Some specific success or error message from AWS SDK.
    @JsonProperty(EXPLANATION)
    public abstract String explanation();

    public static KinesisNewStreamResponse create(@JsonProperty(EXPLANATION) String explanation) {
        return new AutoValue_KinesisNewStreamResponse(explanation);
    }
}