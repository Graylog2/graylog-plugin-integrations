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



import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class SlackClient {

	private static final Logger LOG = LoggerFactory.getLogger(SlackClient.class);

	private final String webhookUrl;
	private final OkHttpClient httpClient;



	public SlackClient(SlackEventNotificationConfig configuration, OkHttpClient httpClient) {
		this.webhookUrl = configuration.webhookUrl();
		this.httpClient = httpClient;
	}

    public void send(SlackMessage message) throws SlackClientException {

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
