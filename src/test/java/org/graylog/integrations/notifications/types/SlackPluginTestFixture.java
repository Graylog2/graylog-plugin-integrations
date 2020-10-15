package org.graylog.integrations.notifications.types;

import com.github.joschi.jadconfig.util.Duration;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.graylog2.shared.bindings.providers.OkHttpClientProvider;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;

public abstract class SlackPluginTestFixture {

    private final MockWebServer server = new MockWebServer();

    public SlackPluginTestFixture() throws IOException {
        server.start();
    }

    MockWebServer getServer() {
        return server;
    }

    OkHttpClient getOkHttpClient() {
        final OkHttpClient client = client(server.url("/").uri());
        return client;
    }

    OkHttpClient client(URI proxyURI) {
        final OkHttpClientProvider provider = new OkHttpClientProvider(
                Duration.milliseconds(100L),
                Duration.milliseconds(100L),
                Duration.milliseconds(100L),
                proxyURI,
                null);

        return provider.get();
    }

    OkHttpClientProvider clientProvider() {
        final OkHttpClientProvider provider = new OkHttpClientProvider(
                Duration.milliseconds(100L),
                Duration.milliseconds(100L),
                Duration.milliseconds(100L),
                server.url("/").uri(),
                null);

        return provider;
    }


}
