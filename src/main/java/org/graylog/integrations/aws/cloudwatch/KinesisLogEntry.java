package org.graylog.integrations.aws.cloudwatch;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import javax.annotation.Nullable;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisLogEntry {

    private static final String LOG_GROUP = "log_group";
    private static final String LOG_STREAM = "log_stream";
    private static final String TIMESTAMP = "timestamp";
    private static final String MESSAGE = "message";

    // Optional, only specified if the message originated from CloudWatch.
    @Nullable
    @JsonProperty(LOG_GROUP)
    public abstract String logGroup();

    // All messages received VIA Kinesis will have a log stream.
    @JsonProperty(LOG_STREAM)
    public abstract String logStream();

    @JsonProperty(TIMESTAMP)
    public abstract long timestamp();

    @JsonProperty(MESSAGE)
    public abstract String message();

    @JsonCreator
    public static KinesisLogEntry create(@JsonProperty(LOG_GROUP) String logGroup,
                                         @JsonProperty(LOG_STREAM) String logStream,
                                         @JsonProperty(TIMESTAMP) long timestamp,
                                         @JsonProperty(MESSAGE) String message) {
        return new AutoValue_KinesisLogEntry(logGroup, logStream, timestamp, message);
    }
}