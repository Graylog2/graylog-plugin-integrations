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


public class SlackClientTest {

    //todo needs to be deprectaed
    private SlackClient slackClient;
    private final MockWebServer server = new MockWebServer();
    private SlackClient okHttpSlackClient;


    @Before
    public void setUp() throws URISyntaxException, IOException {
        server.start();
        SlackEventNotificationConfig slackEventNotificationConfig = SlackEventNotificationConfig.builder()
                .build();
        slackEventNotificationConfig.validate();
        //slackClient = new SlackClient(slackEventNotificationConfig);



        final OkHttpClient client = client(server.url("/").uri());
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

    @Ignore("To be deprecated.")
    @Test(expected = SlackClient.SlackClientException.class)
    public void send_message_with_invalid_webhookurl() throws SlackClient.SlackClientException {
        //to be deprecated.
        SlackMessage message = new SlackMessage("Es war einmal, inmitten eines dichten Waldes, ein kleines Haus");
        slackClient.send(message);
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
        slackClient = null;
    }

    @Test(expected = SlackClient.SlackClientException.class)
    public void send_message_with_okhttpclient_and_invalid_webhookurl() throws SlackClient.SlackClientException {
        SlackMessage message = new SlackMessage("Henry Hühnchen(little chicken)");
        okHttpSlackClient.send_with_okhttp(message);
    }

    private OkHttpClient client(URI proxyURI) {
        final OkHttpClientProvider provider = new OkHttpClientProvider(
                Duration.milliseconds(100L),
                Duration.milliseconds(100L),
                Duration.milliseconds(100L),
                proxyURI,
                null);

        return provider.get();
    }
}
