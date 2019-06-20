package org.graylog.integrations.aws.resources.responses;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;
import org.graylog.integrations.aws.AWSMessageType;

import javax.annotation.Nullable;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisHealthCheckResponse {

    private static final String SUCCESS = "success";
    private static final String INPUT_TYPE = "input_type";
    private static final String EXPLANATION = "explanation";
    private static final String MESSAGE_SUMMARY = "message_summary";

    @JsonProperty(SUCCESS)
    public abstract boolean success();

    // Eg. CloudWatch, other.
    @JsonProperty(INPUT_TYPE)
    public abstract AWSMessageType inputType();

    // Some specific success or error message from AWS SDK.
    @JsonProperty(EXPLANATION)
    public abstract String explanation();

    // A JSON representation of the message. This will be displayed in the UI to show the user
    // that we have identified the message type. The user can then verify that the parsed
    // message looks correct.
    @Nullable
    @JsonProperty(MESSAGE_SUMMARY)
    public abstract String messageSummary();

    public static KinesisHealthCheckResponse create(@JsonProperty(SUCCESS) boolean success,
                                                    @JsonProperty(INPUT_TYPE) AWSMessageType inputType,
                                                    @JsonProperty(EXPLANATION) String explanation,
                                                    @JsonProperty(MESSAGE_SUMMARY) String messageSummary) {
        return new AutoValue_KinesisHealthCheckResponse(success, inputType, explanation, messageSummary);
    }

    /**
     * Create failed/unknown message type response.
     * @return a {@link KinesisHealthCheckResponse} instance
     */
    public static KinesisHealthCheckResponse createFailed(@JsonProperty(EXPLANATION) String explanation,
                                                          @JsonProperty(MESSAGE_SUMMARY) String messageSummary) {
        return new AutoValue_KinesisHealthCheckResponse(false, AWSMessageType.UNKNOWN, explanation, messageSummary);
    }
}