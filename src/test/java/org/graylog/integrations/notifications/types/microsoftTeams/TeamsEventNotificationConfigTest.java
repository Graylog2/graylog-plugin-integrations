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
package org.graylog.integrations.notifications.types.microsoftTeams;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.graylog.events.event.EventDto;
import org.graylog.events.notifications.EventNotificationExecutionJob;
import org.graylog2.contentpacks.EntityDescriptorIds;
import org.graylog2.plugin.rest.ValidationResult;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;


public class TeamsEventNotificationConfigTest {

        @Test
        public void validate_succeeds_whenWebhookUrlIsValidUrl() {
            TeamsEventNotificationConfig teamsEventNotificationConfig = TeamsEventNotificationConfig.builder()
                    .webhookUrl("http://graylog.org")
                    .build();
            ValidationResult result = teamsEventNotificationConfig.validate();
            Map errors = result.getErrors();
            assertThat(errors).size().isEqualTo(0);
        }

        @Test
        public void validate_succeeds_whenWebhookUrlIsValidTeamsUrl() {
            TeamsEventNotificationConfig teamsEventNotificationConfig = TeamsEventNotificationConfig.builder()
                    .webhookUrl("https://teams.webhook.office.com/webhookb2/d6068ba8-584c-41--bb7e-3b112e9b1ff4/IncomingWebhook/440/846c")
                    .build();
            ValidationResult result = teamsEventNotificationConfig.validate();
            Map errors = result.getErrors();
            assertThat(errors).size().isEqualTo(0);
        }



        @Test
        public void validate_failsAndReturnsAnError_whenWebhookUrlIsInvalid() {
            TeamsEventNotificationConfig teamsEventNotificationConfig = TeamsEventNotificationConfig.builder()
                    .webhookUrl("html:/?Thing.foo")
                    .build();
            ValidationResult result = teamsEventNotificationConfig.validate();
            assertThat(result.failed()).isTrue();
            Map errors = result.getErrors();
            assertThat(errors).size().isEqualTo(1);
            assertEquals(((List)errors.get(TeamsEventNotificationConfig.FIELD_WEBHOOK_URL)).get(0),
                    TeamsEventNotificationConfig.INVALID_WEBHOOK_ERROR_MESSAGE);
        }

        @Test
        public void validate_failsAndReturnsAnError_whenWebhookUrlIsInvalidTeamsUrl() {
            TeamsEventNotificationConfig teamsEventNotificationConfig = TeamsEventNotificationConfig.builder()
                    .webhookUrl("https://webhooks.office.com/foo")
                    .build();
            ValidationResult result = teamsEventNotificationConfig.validate();
            assertThat(result.failed()).isTrue();
            Map errors = result.getErrors();
            assertThat(errors).size().isEqualTo(1);
            assertEquals(((List)errors.get(TeamsEventNotificationConfig.FIELD_WEBHOOK_URL)).get(0),
                    TeamsEventNotificationConfig.INVALID_TEAMS_URL_ERROR_MESSAGE);
        }


        @Test
        public void validate_messageBacklog() {
            TeamsEventNotificationConfig negativeBacklogSize = TeamsEventNotificationConfig.builder()
                    .backlogSize(-1)
                    .build();

            assertThat(negativeBacklogSize.webhookUrl()).isEqualTo(TeamsEventNotificationConfig.WEB_HOOK_URL);

            Collection<String> expected = new ArrayList();
            expected.add(TeamsEventNotificationConfig.INVALID_BACKLOG_ERROR_MESSAGE);

            assertThat(negativeBacklogSize.validate().failed()).isTrue();
            Map<String, Collection<String>> errors = negativeBacklogSize.validate().getErrors();
            assertThat(errors.get("backlog_size")).isEqualTo(expected);

            Map<String, Collection<String>> errors1 = negativeBacklogSize.validate().getErrors();
            assertThat(errors1.get("backlog_size")).isEqualTo(expected);


            TeamsEventNotificationConfig goodBacklogSize = TeamsEventNotificationConfig.builder()
                    .backlogSize(5)
                    .build();
            assertThat(goodBacklogSize.validate().failed()).isFalse();

        }


        @Test
        public void toJobTriggerData() {

            final DateTime now = DateTime.parse("2019-01-01T00:00:00.000Z");
            final ImmutableList<String> keyTuple = ImmutableList.of("a", "b");

            final EventDto eventDto = EventDto.builder()
                    .id("01DF119QKMPCR5VWBXS8783799")
                    .eventDefinitionType("aggregation-v1")
                    .eventDefinitionId("54e3deadbeefdeadbeefaffe")
                    .originContext("urn:graylog:message:es:graylog_0:199a616d-4d48-4155-b4fc-339b1c3129b2")
                    .eventTimestamp(now)
                    .processingTimestamp(now)
                    .streams(ImmutableSet.of("000000000000000000000002"))
                    .sourceStreams(ImmutableSet.of("000000000000000000000001"))
                    .message("Test message")
                    .source("source")
                    .keyTuple(keyTuple)
                    .key(String.join("|", keyTuple))
                    .priority(4)
                    .alert(false)
                    .fields(ImmutableMap.of("hello", "world"))
                    .build();

            final TeamsEventNotificationConfig teamsEventNotificationConfig = TeamsEventNotificationConfig.builder().build();
            EventNotificationExecutionJob.Data data = (EventNotificationExecutionJob.Data) teamsEventNotificationConfig.toJobTriggerData(eventDto);
            assertThat(data.type()).isEqualTo(EventNotificationExecutionJob.TYPE_NAME);
            assertThat(data.eventDto().processingTimestamp()).isEqualTo(now);

        }

        @Test(expected = NullPointerException.class)
        public void toContentPackEntity() {
            final TeamsEventNotificationConfig teamsEventNotificationConfig = TeamsEventNotificationConfig.builder().build();
            teamsEventNotificationConfig.toContentPackEntity(EntityDescriptorIds.empty());
        }
}

