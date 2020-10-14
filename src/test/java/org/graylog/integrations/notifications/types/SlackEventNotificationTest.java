package org.graylog.integrations.notifications.types;

import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.events.notifications.NotificationDto;
import org.graylog.events.notifications.NotificationTestData;
import org.graylog.events.notifications.types.HTTPEventNotificationConfig;
import org.graylog2.plugin.MessageSummary;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SlackEventNotificationTest {

    private SlackEventNotification slackEventNotification;
    private SlackEventNotificationConfig slackEventNotificationConfig;
    private EventNotificationContext eventNotificationContext;

    @Before
    public void setUp() {
        slackEventNotificationConfig = AutoValue_SlackEventNotificationConfig.builder().build();

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
        assertThat(message.getBytes().length).isEqualTo(95);
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
        customMessageModel.forEach((k, v) -> System.out.println((k + ":" + v)));
        assertThat(customMessageModel).isNotNull();
        assertThat(customMessageModel.get("event_definition_type")).isEqualTo("slack-notification-v1");
    }
}