package org.graylog.integrations.aws.resources.responses;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

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
    public abstract String message();

    public static KinesisHealthCheckResponse create(@JsonProperty("success") boolean success,
                                                    @JsonProperty("log_type") String logType,
                                                    @JsonProperty("message") String message) {
        return new AutoValue_KinesisHealthCheckResponse(success, logType, message);
    }
}