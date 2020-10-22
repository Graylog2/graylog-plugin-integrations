package org.graylog.integrations.notifications.types;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.graylog.events.notifications.PermanentEventNotificationException;
import org.graylog.events.notifications.TemporaryEventNotificationException;
import org.graylog2.plugin.rest.ValidationResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class SlackClientTest extends SlackPluginTestFixture {

    private SlackClient okHttpSlackClient;

    private MockWebServer server;

    public SlackClientTest() throws IOException {
    }


    @Before
    public void setUp() {
        server= getServer();
        final OkHttpClient client = getOkHttpClient();
        assertThat(client.proxySelector().select(URI.create("http://127.0.0.1/")))
                .hasSize(1)
                .first()
                .matches(proxy -> proxy.type() == Proxy.Type.DIRECT);
        assertThat(client.proxySelector().select(URI.create("http://www.example.com/")))
                .hasSize(1)
                .first()
                .matches(proxy -> proxy.equals(server.toProxyAddress()));
        okHttpSlackClient = new SlackClient(client);

    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test(expected = PermanentEventNotificationException.class)
    public void send_message_with_okhttpclient_and_invalid_webhookurl() throws TemporaryEventNotificationException, PermanentEventNotificationException {
        SlackMessage message = new SlackMessage("Henry Hühnchen(little chicken)");
        SlackEventNotificationConfig slackEventNotificationConfig = SlackEventNotificationConfig.builder()
                .webhookUrl("http://localhost:8080")
                .build();
        okHttpSlackClient.send(message,slackEventNotificationConfig.webhookUrl());
    }




}
