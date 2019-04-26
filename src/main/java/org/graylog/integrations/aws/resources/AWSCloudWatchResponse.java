package org.graylog.integrations.aws.resources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class AWSCloudWatchResponse {

    @JsonProperty
    public abstract boolean success();

    // Eg. CloudWatch, other.
    @JsonProperty
    public abstract String logType();

    // Some specific success or error message from AWS SDK.
    @JsonProperty
    public abstract String message();

    public static AWSCloudWatchResponse create(@JsonProperty("success") boolean success,
                                               @JsonProperty("logType") String logType,
                                               @JsonProperty("message") String message) {
        return new AutoValue_AWSCloudWatchResponse(success, logType, message);
    }
}
