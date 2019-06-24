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
public abstract class CloudWatchLogEntry {

    private static final String LOG_GROUP = "log_group";
    private static final String LOG_STREAM = "log_stream";
    private static final String TIMESTAMP = "timestamp";
    private static final String MESSAGE = "message";

    /**
     * Log Group is optional, since messages may have been written directly to Kinesis without using CloudWatch.
     * Only CloudWatch messages written VIA Kinesis CloudWatch subscriptions will contain a log group.
     */
    @Nullable
    @JsonProperty(LOG_GROUP)
    public abstract String logGroup();

    @JsonProperty(LOG_STREAM)
    public abstract String logStream();

    @JsonProperty(TIMESTAMP)
    public abstract long timestamp();

    @JsonProperty(MESSAGE)
    public abstract String message();

    @JsonCreator
    public static CloudWatchLogEntry create(@JsonProperty(LOG_GROUP) String logGroup,
                                            @JsonProperty(LOG_STREAM) String logStream,
                                            @JsonProperty(TIMESTAMP) long timestamp,
                                            @JsonProperty(MESSAGE) String message) {
        return new AutoValue_CloudWatchLogEntry(logGroup, logStream, timestamp, message);
    }
}