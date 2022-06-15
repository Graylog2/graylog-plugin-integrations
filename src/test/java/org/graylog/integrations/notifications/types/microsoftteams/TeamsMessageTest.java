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
package org.graylog.integrations.notifications.types.microsoftteams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TeamsMessageTest {

    ObjectMapper objectMapper = new ObjectMapper();
    @Test
    public void validateTitle() throws IOException {
        JsonNode customMessage = objectMapper.readTree("{\"name\":\"Type\",\"value\":\"test-dummy-v1\"}");
        String testDescription = "_Event Definition Test Description_";
        String testColor = "#FF0000";
        String testMessageTitle = "**Alert Event Definition Test Title triggered:**";
        String testIconUrl = "http://link.to.image/64/64";
        TeamsMessage.Sections section = TeamsMessage.Sections.builder()
                .activityImage(testIconUrl)
                .activitySubtitle(testDescription)
                .facts(customMessage)
                .build();
        TeamsMessage message = TeamsMessage.builder()
                .color(testColor)
                .text(testMessageTitle)
                .sections(Collections.singleton(section))
                .build();
        String expected = objectMapper.writeValueAsString(message);
        List<String> text = getJsonNodeFieldValue(expected, TeamsMessage.FIELD_TEXT);
        assertThat(text.size()).isEqualTo(1);
        assertThat(text.get(0)).isEqualTo(testMessageTitle);
        List<String> title = getJsonNodeFieldValue(expected, TeamsMessage.FIELD_ACTIVITY_SUBTITLE);
        assertThat(title.size()).isEqualTo(1);
        assertThat(title.get(0)).isEqualTo(testDescription);
        List<String> color = getJsonNodeFieldValue(expected, TeamsMessage.FIELD_THEME_COLOR);
        assertThat(color.size()).isEqualTo(1);
        assertThat(color.get(0)).isEqualTo(testColor);
        List<String> sections = getJsonNodeFieldValue(expected, TeamsMessage.FIELD_SECTIONS);
        assertThat(sections.size()).isEqualTo(1);
        List<String> facts = getJsonNodeFieldValue(expected, TeamsMessage.FIELD_FACTS);
        assertThat(facts.size()).isEqualTo(1);
        List<String> iconUrl = getJsonNodeFieldValue(expected, TeamsMessage.FIELD_ACTIVITY_IMAGE);
        assertThat(iconUrl.size()).isEqualTo(1);
        assertThat(iconUrl.get(0)).isEqualTo(testIconUrl);
        // confirm default values set as expected
        List<String> type = getJsonNodeFieldValue(expected, TeamsMessage.FIELD_TYPE);
        assertThat(type.size()).isEqualTo(1);
        assertThat(type.get(0)).isEqualTo(TeamsMessage.VALUE_TYPE);
        List<String> context = getJsonNodeFieldValue(expected, TeamsMessage.FIELD_CONTEXT);
        assertThat(context.size()).isEqualTo(1);
        assertThat(context.get(0)).isEqualTo(TeamsMessage.VALUE_CONTEXT);
    }

    List<String> getJsonNodeFieldValue(String expected, String fieldName) throws IOException {
        final byte[] bytes = expected.getBytes();
        JsonNode jsonNode = new ObjectMapper().readTree(bytes);
        return jsonNode.findValuesAsText(fieldName);
    }
}
