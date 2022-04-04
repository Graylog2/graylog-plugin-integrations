package org.graylog.integrations.notifications.types.opsGenie;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.graylog.events.notifications.PermanentEventNotificationException;
import org.graylog.events.notifications.TemporaryEventNotificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class OpsGenieClient {
    private static final Logger LOG = LoggerFactory.getLogger(OpsGenieClient.class);
    private final OkHttpClient httpClient;
    private final String opsGenieApiUrl = "https://api.opsgenie.com/v2/alerts";


    @Inject
    public OpsGenieClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }


    /**
//     * @param message alert message to be sent to OpsGenie
//     * @param apiUrl OpsGenie alerts API url
     * @throws TemporaryEventNotificationException - thrown for network or timeout type issues
     * @throws PermanentEventNotificationException - thrown with bad webhook url, authentication error type issues
     */
    public void send(String message, String accessToken) throws TemporaryEventNotificationException, PermanentEventNotificationException {

        final Request request = new Request.Builder()
                .url(opsGenieApiUrl)
                .addHeader(AUTHORIZATION, "GenieKey "+accessToken)
                .addHeader(CONTENT_TYPE,"application/json")
                .post(RequestBody.create(MediaType.parse(APPLICATION_JSON), message))
                .build();

        LOG.debug("Posting to Alerts API <{}> the payload is <{}>",
                opsGenieApiUrl,
                message);

        try (final Response r = httpClient.newCall(request).execute()) {
            if (!r.isSuccessful()) {
                throw new PermanentEventNotificationException(
                        "Expected successful HTTP response [2xx] but got [" + r.code() + "]. " + opsGenieApiUrl);
            }
        } catch (IOException e) {
            throw new TemporaryEventNotificationException("Unable to send the OpsGenie Alert Message. " + e.getMessage());
        }
    }
}
