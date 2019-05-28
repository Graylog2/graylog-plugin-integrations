package org.graylog.integrations.aws;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import org.graylog.autovalue.WithBeanGetter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * TODO: This class has not been used yet. We will delete it if we decide to store data directly in the input.
 * I am currently feeling like we will likely store data in the input.
 */
@AutoValue
@JsonDeserialize(builder = KinesisDTO.Builder.class)
@WithBeanGetter
public abstract class KinesisDTO {


    public static final String FIELD_ID = "id";

    // TODO: Do we really need a title, summary, and description
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_SUMMARY = "summary";
    public static final String FIELD_DESCRIPTION = "description";

    public static final String FIELD_AWS_ACCESS_KEY_ID = "aws_access_key_id";
    public static final String FIELD_AWS_SECRET_ACCESS_KEY = "aws_secret_access_key";
    public static final String FIELD_AWS_KINESIS_STREAM_NAME = "aws_kinesis_stream_name";
    public static final String FIELD_AWS_LOG_TYPE = "aws_log_type";
    public static final String FIELD_AWS_KINESIS_SUBSCRIPTION_ID = "kinesis_subscription_id";
    public static final String FIELD_CREATED_AT = "created_at";

    public static final ImmutableSet<String> SORT_FIELDS = ImmutableSet.of(FIELD_ID, FIELD_TITLE, FIELD_CREATED_AT );

    @ObjectId
    @Id
    @Nullable
    @JsonProperty(FIELD_ID)
    public abstract String id();

    @JsonProperty(FIELD_TITLE)
    @NotBlank
    public abstract String title();

    // A short, one sentence description of the integration
    @JsonProperty(FIELD_SUMMARY)
    public abstract String summary();

    // A longer description of the integration
    @JsonProperty(FIELD_DESCRIPTION)
    public abstract String description();

    // Must be encrypted before being stored.
    @JsonProperty(FIELD_AWS_ACCESS_KEY_ID)
    public abstract String awsAccessKeyId();

    // Must be encrypted before being stored.
    @JsonProperty(FIELD_AWS_SECRET_ACCESS_KEY)
    public abstract String awsSecretAccessKey();

    @JsonProperty(FIELD_AWS_KINESIS_STREAM_NAME)
    public abstract String awsKinesisStreamName();

    @JsonProperty(FIELD_AWS_LOG_TYPE)
    public abstract String awsLogType();

    @JsonProperty(FIELD_AWS_KINESIS_SUBSCRIPTION_ID)
    public abstract String awsKinesisSubscriptionId();

    @JsonProperty(FIELD_CREATED_AT)
    public abstract DateTime createdAt();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder title(@NotBlank String title);

        public abstract Builder summary(String summary);

        public abstract Builder description(String description);

        public abstract Builder awsAccessKeyId(String awsAccessKeyId);

        public abstract Builder awsSecretAccessKey(String awsSecretAccessKey);

        public abstract Builder awsKinesisStreamName(String awsKinesisStreamName);

        public abstract Builder awsLogType(String awsLogType);

        public abstract Builder awsKinesisSubscriptionId(String awsKinesisSubscriptionId);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract KinesisDTO build();
    }
}