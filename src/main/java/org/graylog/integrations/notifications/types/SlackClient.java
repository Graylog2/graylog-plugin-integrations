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
package org.graylog.integrations.notifications.types;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.joschi.jadconfig.util.Duration;
import com.google.common.collect.ImmutableList;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.graylog.events.notifications.*;
import org.graylog.events.notifications.types.HTTPEventNotificationConfig;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.shared.bindings.providers.OkHttpClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.*;
import java.nio.charset.StandardCharsets;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class SlackClient {

	private static final Logger LOG = LoggerFactory.getLogger(SlackClient.class);

	private final String webhookUrl;
	private String proxyURL;
	private final OkHttpClient httpClient;



	public SlackClient(SlackEventNotificationConfig configuration, OkHttpClient httpClient) {
		this.webhookUrl = configuration.webhookUrl();
		this.httpClient = httpClient;
	}

    // TODO: 9/8/20
    //We usually use okhttp for all HTTP connections so I think we should inject an OkHttpClient instance here
    // and use that instead of the Java core HTTP client.
    //This also has the benefit of automatic proxy configuration based on the settings in graylog.conf so the user
    // doesn't need to configure the proxy server in the slack notification settings.
    public void send_with_okhttp(SlackMessage message) throws SlackClientException {

		final Request request = new Request.Builder()
				.url(webhookUrl)
				.post(RequestBody.create(MediaType.parse(APPLICATION_JSON), message.getJsonString()))
				.build();

		LOG.debug("Posting to webhook url <{}> the paylod is <{}>",
					webhookUrl,
				    message.getJsonString());

		try (final Response r = httpClient.newCall(request).execute()) {
			if (!r.isSuccessful()) {
				throw new SlackClientException(
						"Expected successful HTTP response [2xx] but got [" + r.code() + "]. " + webhookUrl);
			}
		} catch (IOException e) {
			throw new SlackClientException("exception" +e);
		}
    }



	public static class SlackClientException extends Exception {

		public SlackClientException(String msg) {
			super(msg);
		}

		public SlackClientException(String msg, Throwable cause) {
			super(msg, cause);
		}

	}



}
