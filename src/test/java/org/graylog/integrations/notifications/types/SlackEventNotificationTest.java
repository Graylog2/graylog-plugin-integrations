package org.graylog.integrations.notifications.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.bson.types.ObjectId;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.events.notifications.NotificationDto;
import org.graylog.events.notifications.NotificationTestData;
import org.graylog.events.notifications.types.HTTPEventNotificationConfig;
import org.graylog.integrations.notifications.modeldata.StreamModelData;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamImpl;
import org.graylog2.streams.StreamService;
import org.graylog2.streams.StreamServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlackEventNotificationTest {

    private SlackEventNotification slackEventNotification;
    private SlackEventNotificationConfig slackEventNotificationConfig;
    private EventNotificationContext eventNotificationContext;



    @Before
    public void setUp() {

        slackEventNotificationConfig = new AutoValue_SlackEventNotificationConfig.Builder()
                                       .notifyChannel(true)
                                       .type(SlackEventNotificationConfig.TYPE_NAME)
                                       .color("#FF2052")
                                       .webhookUrl("a webhook url")
                                       .channel("#general")
                                       .customMessage("a custom message")
                                       .backlogItemMessage("this is a backlog item message")
                                       .linkNames(true)
                                       .graylogUrl("http://localhost:8080")
                                       .build();

        slackEventNotificationConfig.validate();
        //todo: make method `getDummyContext` public
        eventNotificationContext = NotificationTestData.getDummyContext(getHttpNotification(), "ayirp");
        //todo : research how to create a new insatnce of SlackEventNotification without a mock
        slackEventNotification = new SlackEventNotification();

    }

    private NotificationDto getHttpNotification() {
        return NotificationDto.builder()
                .title("Foobar")
                .id("1234")
                .description("")
                .config(HTTPEventNotificationConfig.Builder.create()
                        .url("http://localhost")
                        .build())
                .build();
    }



    @Test
    public void createSlackMessage() {
        slackEventNotification.createSlackMessage(eventNotificationContext, slackEventNotificationConfig);
    }

    @After
    public void tearDown() {
        slackEventNotification = null;
        slackEventNotificationConfig = null;
        eventNotificationContext = null;
    }

    @Test
    public void buildDefaultMessage() {
        String message =  slackEventNotification.buildDefaultMessage(eventNotificationContext,slackEventNotificationConfig);
        assertThat(message).isNotBlank();
        assertThat(message).isNotEmpty();
        assertThat(message).isNotNull();
        assertThat(message).contains("@channel");
        assertThat(message.getBytes().length).isEqualTo(117);
        assertThat(message);
    }

    @Test
    public void getAlarmBacklog() {
        List<MessageSummary> messageSummaries =  slackEventNotification.getAlarmBacklog(eventNotificationContext);
        assertThat(messageSummaries.size()).isEqualTo(1);
    }

    @Test
    public void getCustomMessageModel() {
        List<MessageSummary> messageSummaries = slackEventNotification.getAlarmBacklog(eventNotificationContext);
        Map<String, Object> customMessageModel = slackEventNotification.getCustomMessageModel(eventNotificationContext, slackEventNotificationConfig, messageSummaries);
        assertThat(customMessageModel).isNotNull();
        assertThat(customMessageModel.get("event_definition_type")).isEqualTo("slack-notification-v1");
    }

    @Test
    public void buildStreamWithUrl() {
        final ObjectId streamId = new ObjectId("5628f4503b0c5756a8eebc4d");
        final Stream stream = mock(Stream.class);
        when(stream.getId()).thenReturn(streamId.toHexString());
        when(stream.getTitle()).thenReturn("title");
        when(stream.getDescription()).thenReturn("description");
        slackEventNotification.buildStreamWithUrl(stream,eventNotificationContext,slackEventNotificationConfig);
    }
}