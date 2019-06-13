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

    private static final String SUCCESS = "success";
    private static final String LOG_TYPE = "log_type";
    private static final String EXPLANATION = "explanation";
    private static final String MESSAGE_SUMMARY = "message_summary";

    @JsonProperty(SUCCESS)
    public abstract boolean success();

    // Eg. CloudWatch, other.
    @JsonProperty(LOG_TYPE)
    public abstract String logType();

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
                                                    @JsonProperty(LOG_TYPE) String logType,
                                                    @JsonProperty(EXPLANATION) String explanation,
                                                    @JsonProperty(MESSAGE_SUMMARY) String messageSummary) {
        return new AutoValue_KinesisHealthCheckResponse(success, logType, explanation, messageSummary);
    }
}