package org.graylog.integrations.aws.resources.responses;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisNewStreamResponse {

    private static final String STREAM_NAME = "stream_name";
    private static final String STREAM_ARN = "stream_arn";
    private static final String EXPLANATION = "explanation";

    @JsonProperty(STREAM_NAME)
    public abstract String streamName();

    @JsonProperty(STREAM_ARN)
    public abstract String streamArn();

    // Some specific success or error message from AWS SDK.
    @JsonProperty(EXPLANATION)
    public abstract String explanation();

    public static KinesisNewStreamResponse create(@JsonProperty(STREAM_NAME) String streamName,
                                                  @JsonProperty(STREAM_ARN) String streamArn,
                                                  @JsonProperty(EXPLANATION) String explanation) {
        return new AutoValue_KinesisNewStreamResponse(streamName, streamArn, explanation);
    }
}