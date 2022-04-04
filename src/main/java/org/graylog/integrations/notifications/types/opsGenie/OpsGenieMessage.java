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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OpsGenieMessage {

    private String[] tags;
    private String priority;
    private String userName;
    private final String messageTitle;
    private Map<String, String> customMessage;
    private String description;
    private String[] responders;


    public OpsGenieMessage(
            String messageTitle,
            Map<String, String> customMessage,
            String description,
            String userName,
            String[] tags,
            String priority,
            String[] responders
    ) {
        this.priority = priority;
        this.tags = tags;
        this.userName = userName;
        this.messageTitle = messageTitle;
        this.customMessage = customMessage;
        this.description = description;
        this.responders = responders;

    }

    public OpsGenieMessage(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public String getJsonString() {

        final Map<String, Object> params = new HashMap<>();
        params.put("message", messageTitle);
        params.put("tags", tags);
        params.put("details", customMessage);
        params.put("user", userName);
        params.put("priority",priority);
        params.put("description", description);

        final List<ResponderField> res = new ArrayList<>();

        for (String team : responders) {
            final ResponderField responderField = new ResponderField(
                    "team",
                    team
            );
            res.add(responderField);
        }
            params.put("responders", res);

        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not build payload JSON.", e);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponderField {
        @JsonProperty
        public String type;
        @JsonProperty
        public String name;

        @JsonCreator
        public ResponderField(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

}
