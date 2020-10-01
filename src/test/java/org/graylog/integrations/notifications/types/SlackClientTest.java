package org.graylog.integrations.notifications.types;

import org.junit.Before;
import org.junit.Test;

//https://hooks.slack.com/services/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

public class SlackClientTest {

    private SlackClient slackClient;

    @Before
    public void setUp() {
        SlackEventNotificationConfig slackEventNotificationConfig = SlackEventNotificationConfig.builder()
                .build();
        slackEventNotificationConfig.validate();
        slackClient = new SlackClient(slackEventNotificationConfig);
    }

    @Test(expected = SlackClient.SlackClientException.class)
    public void test_sending_message_to_invalid_webhookurl() throws SlackClient.SlackClientException {
        SlackMessage message = new SlackMessage("Es war einmal, inmitten eines dichten Waldes, ein kleines Haus");
        slackClient.send(message);
    }
}
