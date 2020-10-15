package org.graylog.integrations.notifications.types;

import com.github.joschi.jadconfig.util.Duration;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.graylog2.shared.bindings.providers.OkHttpClientProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;


public class SlackClientTest extends SlackPluginTestFixture {

    private SlackClient okHttpSlackClient;
    private MockWebServer server;

    public SlackClientTest() throws IOException {
    }


    @Before
    public void setUp() {
        server= getServer();
        SlackEventNotificationConfig slackEventNotificationConfig = SlackEventNotificationConfig.builder()
                .build();
        slackEventNotificationConfig.validate();
        final OkHttpClient client = getOkHttpClient();
        assertThat(client.proxySelector().select(URI.create("http://127.0.0.1/")))
                .hasSize(1)
                .first()
                .matches(proxy -> proxy.type() == Proxy.Type.DIRECT);
        assertThat(client.proxySelector().select(URI.create("http://www.example.com/")))
                .hasSize(1)
                .first()
                .matches(proxy -> proxy.equals(server.toProxyAddress()));
        okHttpSlackClient = new SlackClient(slackEventNotificationConfig,client);

    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test(expected = SlackClient.SlackClientException.class)
    public void send_message_with_okhttpclient_and_invalid_webhookurl() throws SlackClient.SlackClientException {
        SlackMessage message = new SlackMessage("Henry Hühnchen(little chicken)");
        okHttpSlackClient.send_with_okhttp(message);
    }


}
