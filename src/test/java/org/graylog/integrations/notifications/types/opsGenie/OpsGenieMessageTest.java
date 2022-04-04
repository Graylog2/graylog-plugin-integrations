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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OpsGenieMessageTest {

    @Test
    public void test_good_title() throws IOException {
        Map<String, String> customMessage = new HashMap<>();
        customMessage.put("key1", "value1");
        String[] responders = {"team1", "team2"};
        String[] tags = {"tag1", "tag2"};
        OpsGenieMessage message = new OpsGenieMessage("messageTitle", customMessage,"description", "userName", tags, "priority",responders);
        String expected = message.getJsonString();
        List<String> text = getJsonNodeFieldValue(expected, "message");
        assertThat(text).isNotEmpty();
        assertThat(text).isNotNull();
    }

    List<String> getJsonNodeFieldValue(String expected, String fieldName) throws IOException {
        final byte[] bytes = expected.getBytes();
        JsonNode jsonNode = new ObjectMapper().readTree(bytes);
        return jsonNode.findValuesAsText(fieldName);
    }

}
