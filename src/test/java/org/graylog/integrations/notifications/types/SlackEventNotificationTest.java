package org.graylog.integrations.notifications.types;

import com.floreysoft.jmte.Engine;
import com.google.common.collect.ImmutableList;
import org.bson.types.ObjectId;
import org.graylog.events.notifications.*;
import org.graylog.events.notifications.types.HTTPEventNotificationConfig;
import org.graylog2.notifications.Notification;
import org.graylog2.notifications.NotificationImpl;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.shared.bindings.providers.ObjectMapperProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SlackEventNotificationTest extends SlackClientTest {

    @Mock
    NodeId mockNodeId;
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
        eventNotificationContext = NotificationTestData.getDummyContext(getHttpNotification(), "ayirp").toBuilder().notificationConfig(slackEventNotificationConfig).build();


        final ImmutableList<MessageSummary> messageSummaries = ImmutableList.of(
                new MessageSummary("graylog_1", new Message("Test message 1", "source1", new DateTime(2020, 9, 6, 17, 0, DateTimeZone.UTC))),
                new MessageSummary("graylog_2", new Message("Test message 2", "source2", new DateTime(2020, 9, 6, 17, 0, DateTimeZone.UTC)))
        );
        EventNotificationService notificationCallbackService = mock(EventNotificationService.class);
        when(notificationCallbackService.getBacklogForEvent(eventNotificationContext)).thenReturn(messageSummaries);

        NotificationService mockNotificationService = mock(NotificationService.class);
        Notification notification = new NotificationImpl();
        when(mockNotificationService.buildNow()).thenReturn(notification);

        slackEventNotification = new SlackEventNotification(notificationCallbackService, new ObjectMapperProvider().get(),
                Engine.createEngine(),
                mockNotificationService, clientProvider(), mockNodeId);

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
        String message = slackEventNotification.buildDefaultMessage(eventNotificationContext, slackEventNotificationConfig);
        assertThat(message).isNotBlank();
        assertThat(message).isNotEmpty();
        assertThat(message).isNotNull();
        assertThat(message).contains("@channel");
        assertThat(message.getBytes().length).isEqualTo(117);
    }

    @Test
    public void getAlarmBacklog() {
        List<MessageSummary> messageSummaries = slackEventNotification.getAlarmBacklog(eventNotificationContext);
        messageSummaries.forEach(e -> System.out.println(e.getRawMessage()));
        assertThat(messageSummaries.size()).isEqualTo(2);
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
        slackEventNotification.buildStreamWithUrl(stream, eventNotificationContext, slackEventNotificationConfig);
    }

    @Test(expected = PermanentEventNotificationException.class)
    public void execute() throws PermanentEventNotificationException {
        slackEventNotification.execute(eventNotificationContext);
    }
}