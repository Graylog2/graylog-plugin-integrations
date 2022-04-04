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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import org.graylog.events.contentpack.entities.EventNotificationConfigEntity;
import org.graylog.events.notifications.EventNotificationConfig;
import org.graylog2.contentpacks.model.entities.EntityDescriptor;
import org.graylog2.contentpacks.model.entities.references.ValueReference;

import java.util.Map;

@AutoValue
@JsonTypeName(OpsGenieEventNotificationConfigEntity.TYPE_NAME)
@JsonDeserialize(builder = OpsGenieEventNotificationConfigEntity.Builder.class)
public abstract class OpsGenieEventNotificationConfigEntity implements EventNotificationConfigEntity {

    public static final String TYPE_NAME = "OpsGenie-notification-v1";

    @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_ACCESSTOKEN)
    public abstract ValueReference accessToken();

    @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_CHANNEL)
    public abstract ValueReference channel();

    @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_USER_NAME)
    public abstract ValueReference userName();

    @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_TAGS)
    public abstract ValueReference tags();

    @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_MAIN_MESSAGE)
    public abstract ValueReference mainFields();

    @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_CUSTOM_MESSAGE)
    public abstract ValueReference customMessage();

    public static Builder builder() {
        return Builder.create();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public static abstract class Builder implements EventNotificationConfigEntity.Builder<Builder> {

        @JsonCreator
        public static Builder create() {
            return new AutoValue_OpsGenieEventNotificationConfigEntity.Builder()
                    .type(TYPE_NAME);
        }

        @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_ACCESSTOKEN)
        public abstract Builder accessToken(ValueReference accessToken);

        @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_MAIN_MESSAGE)
        public abstract Builder mainFields(ValueReference mainFields);

        @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_CHANNEL)
        public abstract Builder channel(ValueReference channel);

        @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_USER_NAME)
        public abstract Builder userName(ValueReference userName);


        @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_TAGS)
        public abstract Builder tags(ValueReference tags);

        @JsonProperty(OpsGenieEventNotificationConfig.OPSGENIE_CUSTOM_MESSAGE)
        public abstract Builder customMessage(ValueReference customMessage);

        public abstract OpsGenieEventNotificationConfigEntity build();
    }

    @Override
    public EventNotificationConfig toNativeEntity(Map<String, ValueReference> parameters, Map<EntityDescriptor, Object> nativeEntities) {
        return OpsGenieEventNotificationConfig.builder()
                .accessToken(accessToken().asString(parameters))
                .channel(channel().asString(parameters))
                .tags(tags().asString(parameters))
                .mainFields(mainFields().asString(parameters))
                .userName(userName().asString(parameters))
                .customMessage(customMessage().asString(parameters))
                .customMessage(customMessage().asString(parameters))
                .build();
    }
}

