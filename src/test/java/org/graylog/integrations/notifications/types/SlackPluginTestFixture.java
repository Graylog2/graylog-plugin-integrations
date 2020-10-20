package org.graylog.integrations.notifications.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.joschi.jadconfig.util.Duration;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.graylog2.shared.bindings.providers.OkHttpClientProvider;

import java.io.IOException;
import java.util.List;


public abstract class SlackPluginTestFixture {

    private final MockWebServer server = new MockWebServer();

    public SlackPluginTestFixture() throws IOException {
        server.start();
    }

    MockWebServer getServer() {
        return server;
    }

    OkHttpClient getOkHttpClient() {

        final OkHttpClient client = getOkHttpClientProvider().get();
        return client;
    }

    OkHttpClientProvider getOkHttpClientProvider() {
        final OkHttpClientProvider provider = new OkHttpClientProvider(
                Duration.milliseconds(100L),
                Duration.milliseconds(100L),
                Duration.milliseconds(100L),
                server.url("/").uri(),
                null);

        return provider;
    }

    List<String> getJsonNodeFieldValue(String expected,String fieldName) throws IOException {
        final byte[] bytes = expected.getBytes();
        JsonNode jsonNode = new ObjectMapper().readTree(bytes);
        return jsonNode.findValuesAsText(fieldName);
    }

}
