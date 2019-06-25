package org.graylog.integrations.aws.cloudwatch;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import java.util.Optional;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class CloudWatchLogEntry {

    private static final String LOG_GROUP = "log_group";
    private static final String LOG_STREAM = "log_stream";
    private static final String TIMESTAMP = "timestamp";
    private static final String MESSAGE = "message";

    /**
     * CloudWatch Log Group and Log Stream are optional, since messages may have been written directly to Kinesis
     * without using CloudWatch. Only CloudWatch messages written VIA Kinesis CloudWatch subscriptions will
     * contain a log group and stream.
     *
     * @see <a href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/SubscriptionFilters.html">Using CloudWatch Logs Subscription Filters</a>
     */
    @JsonProperty(LOG_GROUP)
    public abstract Optional<String> logGroup();

    @JsonProperty(LOG_STREAM)
    public abstract Optional<String> logStream();

    @JsonProperty(TIMESTAMP)
    public abstract long timestamp();

    @JsonProperty(MESSAGE)
    public abstract String message();

    @JsonCreator
    public static CloudWatchLogEntry create(@JsonProperty(LOG_GROUP) String logGroup,
                                            @JsonProperty(LOG_STREAM) String logStream,
                                            @JsonProperty(TIMESTAMP) long timestamp,
                                            @JsonProperty(MESSAGE) String message) {
        return new AutoValue_CloudWatchLogEntry(new Optional<>(logGroup), new Optional<>(logStream),
                                                timestamp, message);
    }
}