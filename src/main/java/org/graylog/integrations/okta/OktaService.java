package org.graylog.integrations.okta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.graylog2.inputs.InputService;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.shared.inputs.MessageInputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OktaService {

    private static final Logger LOG = LoggerFactory.getLogger(OktaService.class);

    private final InputService inputService;
    private final MessageInputFactory messageInputFactory;
    private final NodeId nodeId;
    private final ObjectMapper objectMapper;

    @Inject
    public OktaService(InputService inputService, MessageInputFactory messageInputFactory, NodeId nodeId,
                       ObjectMapper objectMapper) {

        this.inputService = inputService;
        this.messageInputFactory = messageInputFactory;
        this.nodeId = nodeId;
        this.objectMapper = objectMapper;
    }

    public OktaResponse getSystemLogs(String url, String apiKey) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", apiKey)
                .build();

        okhttp3.Response response = client.newCall(request).execute();
        OktaResponse oktaResponse = OktaResponse.create(response.body().string());
        return oktaResponse;
    }
}