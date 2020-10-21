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
package org.graylog.integrations.pagerduty.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.integrations.pagerduty.dto.PagerDutyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Locale;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * The Pager Duty REST client implementation class compatible with events V2. For more information
 * about the event structure please see
 * <a href="https://v2.developer.pagerduty.com/v2/docs/send-an-event-events-api-v2">the api</a>.
 *
 * This class is heavily based on the work committed by Jochen, James, Dennis, Padma, and Edgar
 * <a href="https://github.com/graylog-labs/graylog-plugin-pagerduty/">here</a>.
 *
 * @author Jochen Schalanda
 * @author James Carr
 * @author Dennis Oelkers
 * @author Padma Liyanage
 * @author Edgar Molina
 */
public class PagerDutyClient {
    private static final Logger LOG = LoggerFactory.getLogger(PagerDutyClient.class);

    private static final String API_URL = "https://events.pagerduty.com/v2/enqueue";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final MessageFactory messageFactory;

    @Inject
    public PagerDutyClient(final OkHttpClient httpClient,
                    final ObjectMapper objectMapper,
                    final MessageFactory messageFactory) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.messageFactory = messageFactory;
    }

    public PagerDutyResponse trigger(EventNotificationContext ctx) throws PagerDutyClientException {
        final String payloadString = buildRequestBody(ctx);
        final Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(MediaType.parse(APPLICATION_JSON), payloadString))
                .build();

        LOG.debug("Triggering event in PagerDuty with context: {}", ctx);
        LOG.info("PagerDuty API POST payload: {}", payloadString);

        try (final Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new PagerDutyClientException(String.format(Locale.US,
                        "Received HTTP %d response when sending POST request to Pager Duty API", response.code()));
            }
            return objectMapper.readValue(response.body().string(), PagerDutyResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException("There was an error sending the notification event.", e);
        }
    }

    private String buildRequestBody(EventNotificationContext ctx) throws PagerDutyClientException {
        try {
            return objectMapper.writeValueAsString(messageFactory.createTriggerMessage(ctx));
        } catch (IOException e) {
            throw new PagerDutyClientException("Failed to build trigger message", e);
        }
    }

    public static class PagerDutyClientException extends Exception {
        public PagerDutyClientException(String msg) {
            super(msg);
        }

        public PagerDutyClientException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}