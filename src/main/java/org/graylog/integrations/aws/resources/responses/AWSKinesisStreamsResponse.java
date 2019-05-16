package org.graylog.integrations.aws.resources.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import java.util.List;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class AWSKinesisStreamsResponse {

    // A String list of log group names.
    @JsonProperty
    public abstract List<String> streamNames();

    // Indicates if the request for log groups was successful or not.
    @JsonProperty
    public abstract boolean success();

    // An error message if the request for log groups was unsuccessful
    @JsonProperty
    public abstract String message();

    public static AWSKinesisStreamsResponse create(@JsonProperty("streamNames") List<String> streamNames,
                                                   @JsonProperty("success") boolean success,
                                                   @JsonProperty("message") String message) {
        return new AutoValue_AWSKinesisStreamsResponse(streamNames, success, message);
    }
}