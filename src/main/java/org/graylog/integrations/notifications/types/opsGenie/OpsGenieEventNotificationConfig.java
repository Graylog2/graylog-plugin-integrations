/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package org.graylog.integrations.notifications.types.opsGenie;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import org.graylog.events.contentpack.entities.EventNotificationConfigEntity;
import org.graylog.events.event.EventDto;
import org.graylog.events.notifications.EventNotificationConfig;
import org.graylog.events.notifications.EventNotificationExecutionJob;
import org.graylog.scheduler.JobTriggerData;
import org.graylog2.contentpacks.EntityDescriptorIds;
import org.graylog2.contentpacks.model.entities.references.ValueReference;
import org.graylog2.plugin.rest.ValidationResult;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

@AutoValue
@JsonTypeName(OpsGenieEventNotificationConfig.TYPE_NAME)
@JsonDeserialize(builder = OpsGenieEventNotificationConfig.Builder.class)
public abstract class OpsGenieEventNotificationConfig implements EventNotificationConfig {

    public static final String TYPE_NAME = "opsgenie-notification-v1";

    private static final String DEFAULT_CUSTOM_MESSAGE = "Graylog OpsGenie Notification";
    private static final String DEFAULT_ALERT_MESSAGE = "Graylog OpsGenie Alert";

    private static final long DEFAULT_BACKLOG_SIZE = 0;

    static final String INVALID_BACKLOG_ERROR_MESSAGE = "Backlog size cannot be less than zero";
    static final String ACCESS_TOKEN = "access_token";
    static final String TAGS = "tags";
    static final String INVALID_ACCESSTOKEN_ERROR_MESSAGE = "API Access Token is mandatory";


    static final String OPSGENIE_ACCESSTOKEN = "access_token";
    static final String OPSGENIE_MAIN_MESSAGE = "main_fields";
    static final String OPSGENIE_CUSTOM_MESSAGE = "custom_message";
    static final String OPSGENIE_BACKLOG_SIZE = "backlog_size";
    static final String OPSGENIE_CHANNEL = "channel";
    static final String OPSGENIE_TAGS = "tags";
    static final String OPSGENIE_USER_NAME = "user_name";
    static final String CHANNEL = "#general";

    @JsonProperty(OPSGENIE_BACKLOG_SIZE)
    public abstract long backlogSize();

    @JsonProperty(OPSGENIE_ACCESSTOKEN)
    @NotBlank
    public abstract String accessToken();

    @JsonProperty(OPSGENIE_MAIN_MESSAGE)
    public abstract String mainFields();

    @JsonProperty(OPSGENIE_CUSTOM_MESSAGE)
    public abstract String customMessage();

    @JsonProperty(OPSGENIE_USER_NAME)
    @Nullable
    public abstract String userName();

    @JsonProperty(OPSGENIE_TAGS)
    @Nullable
    public abstract String tags();

    @JsonProperty(OPSGENIE_CHANNEL)
    public abstract String channel();


    @Override
    @JsonIgnore
    public JobTriggerData toJobTriggerData(EventDto dto) {
        return EventNotificationExecutionJob.Data.builder().eventDto(dto).build();
    }

    public static Builder builder() {
        return Builder.create();
    }

    @Override
    @JsonIgnore
    public ValidationResult validate() {
        ValidationResult validation = new ValidationResult();

        if(accessToken().isEmpty()){
            validation.addError(ACCESS_TOKEN, INVALID_ACCESSTOKEN_ERROR_MESSAGE);
        }

        if (backlogSize() < 0) {
            validation.addError(OPSGENIE_BACKLOG_SIZE, INVALID_BACKLOG_ERROR_MESSAGE);
        }
        return validation;
    }

    @AutoValue.Builder
    public static abstract class Builder implements EventNotificationConfig.Builder<Builder> {
        @JsonCreator
        public static Builder create() {

            return new AutoValue_OpsGenieEventNotificationConfig.Builder()
                    .type(TYPE_NAME)
                    .accessToken(ACCESS_TOKEN)
                    .channel(CHANNEL)
                    .mainFields(DEFAULT_ALERT_MESSAGE)
                    .tags(TAGS)
                    .customMessage(DEFAULT_CUSTOM_MESSAGE)
                    .backlogSize(DEFAULT_BACKLOG_SIZE);
        }

        @JsonProperty(OPSGENIE_ACCESSTOKEN)
        public abstract Builder accessToken(String accessToken);

        @JsonProperty(OPSGENIE_CUSTOM_MESSAGE)
        public abstract Builder customMessage(String customMessage);

        @JsonProperty(OPSGENIE_MAIN_MESSAGE)
        public abstract Builder mainFields(String mainFields);

        @JsonProperty(OPSGENIE_USER_NAME)
        public abstract Builder userName(String userName);

        @JsonProperty(OPSGENIE_TAGS)
        public abstract Builder tags(String tags);

        @JsonProperty(OPSGENIE_CHANNEL)
        public abstract Builder channel(String channel);


        @JsonProperty(OPSGENIE_BACKLOG_SIZE)
        public abstract Builder backlogSize(long backlogSize);

        public abstract OpsGenieEventNotificationConfig build();
    }

    @Override
    public EventNotificationConfigEntity toContentPackEntity(EntityDescriptorIds entityDescriptorIds) {
        return OpsGenieEventNotificationConfigEntity.builder()
                .accessToken(ValueReference.of(accessToken()))
                .userName(ValueReference.of(userName()))
                .tags(ValueReference.of(tags()))
                .mainFields(ValueReference.of(mainFields()))
                .channel(ValueReference.of(channel()))
                .customMessage(ValueReference.of(customMessage()))
                .build();
    }
}
