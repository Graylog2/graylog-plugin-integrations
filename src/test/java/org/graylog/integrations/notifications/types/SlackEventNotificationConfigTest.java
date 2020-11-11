package org.graylog.integrations.notifications.types;

import org.graylog2.plugin.rest.ValidationResult;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
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


        assertThat(negativeBacklogSize.channel()).isEqualTo(SlackEventNotificationConfig.CHANNEL);
        assertThat(negativeBacklogSize.webhookUrl()).isEqualTo(SlackEventNotificationConfig.WEB_HOOK_URL);

        Collection<String> expected = new ArrayList();
        expected.add(SlackEventNotificationConfig.INVALID_BACKLOG_ERROR_MESSAGE);

        assertThat(negativeBacklogSize.validate().failed()).isTrue();
        Map<String, Collection<String>> errors = negativeBacklogSize.validate().getErrors();
        assertThat(errors.get("backlog_size")).isEqualTo(expected);


        SlackEventNotificationConfig veryBigBacklogSize =  SlackEventNotificationConfig.builder()
                .backlogSize(99999)
                .build();
        assertThat(veryBigBacklogSize.validate().failed()).isTrue();
        Map<String, Collection<String>> errors1 = negativeBacklogSize.validate().getErrors();
        assertThat(errors1.get("backlog_size")).isEqualTo(expected);


        SlackEventNotificationConfig goodBacklogSize =  SlackEventNotificationConfig.builder()
                .backlogSize(5)
                .build();
        assertThat(goodBacklogSize.validate().failed()).isFalse();

    }


}
