package org.graylog.integrations.notifications.types;

import org.graylog2.plugin.rest.ValidationResult;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class SlackEventNotificationConfigTest {

    @Test
    public void validate_succeeds_whenWebhookUrlIsValid() {
        SlackEventNotificationConfig slackEventNotificationConfig = SlackEventNotificationConfig.builder()
                .webhookUrl("https://hooks.slack.com/services/xxxx/xxxx/xxxxxxx")
                .build();
        ValidationResult result = slackEventNotificationConfig.validate();
        Map errors = result.getErrors();
        assertThat(errors).size().isEqualTo(0);
    }

    @Test
    public void validate_failsAndReturnsAnError_whenWebhookUrlIsInvalid() {
        SlackEventNotificationConfig slackEventNotificationConfig = SlackEventNotificationConfig.builder()
                .webhookUrl("A67888900000")
                .build();
        ValidationResult result = slackEventNotificationConfig.validate();
        assertThat(result.failed()).isTrue();
        Map errors = result.getErrors();
        assertThat(errors).size().isGreaterThan(0);
    }

    @Test
    public void validate_messageBacklog() {
        SlackEventNotificationConfig negativeBacklogSize = SlackEventNotificationConfig.builder()
                                                          .backlogSize(-1)
                                                          .build();
        assertThat(negativeBacklogSize.validate().getErrors().get(SlackEventNotificationConfig.INVALID_BACKLOG_ERROR_MESSAGE));

        SlackEventNotificationConfig veryBigBacklogSize =  SlackEventNotificationConfig.builder()
                                                          .backlogSize(50)
                                                          .build();
        assertThat(veryBigBacklogSize.validate().getErrors().get(SlackEventNotificationConfig.INVALID_BACKLOG_ERROR_MESSAGE));

        SlackEventNotificationConfig goodBacklogSize =  SlackEventNotificationConfig.builder()
                                                       .backlogSize(5)
                                                       .build();
        assertThat(goodBacklogSize.validate().failed()).isFalse();

    }


}
