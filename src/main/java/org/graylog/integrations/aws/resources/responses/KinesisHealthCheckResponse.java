package org.graylog.integrations.aws.resources.responses;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import javax.annotation.Nullable;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisHealthCheckResponse {

    @JsonProperty
    public abstract boolean success();

    // Eg. CloudWatch, other.
    @JsonProperty
    public abstract String logType();

    // Some specific success or error message from AWS SDK.
    @JsonProperty
    public abstract String explanation();

    // A JSON representation of the message. This will be displayed in the UI to show the user
    // that we have identified the message type. The user can then verify that the parsed
    // message looks correct.
    @JsonProperty
    @Nullable
    public abstract String jsonMessage();

    public static KinesisHealthCheckResponse create(@JsonProperty("success") boolean success,
                                                    @JsonProperty("log_type") String logType,
                                                    @JsonProperty("explanation") String explanation,
                                                    @JsonProperty("json_message") String jsonMessage) {
        return new AutoValue_KinesisHealthCheckResponse(success, logType, explanation, jsonMessage);
    }
}