package org.graylog.integrations.notifications.types;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


public class SlackClientTest {

    @Before
    public void setUp() throws Exception {
        SlackEventNotificationConfig slackEventNotificationConfig = SlackEventNotificationConfig.builder()
                         .build();
        slackEventNotificationConfig.validate();

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void send1() {
    }

    @Test
    public void send() {
    }
}
