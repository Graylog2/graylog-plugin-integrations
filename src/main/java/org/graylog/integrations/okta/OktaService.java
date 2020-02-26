/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.integrations.okta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.graylog2.inputs.InputService;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.shared.inputs.MessageInputFactory;

import java.io.IOException;

public class OktaService {

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

    public OktaResponse getSystemLogs(String domain, String apiKey) throws IOException {
        // TODO check if client is suitable
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(domain)
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