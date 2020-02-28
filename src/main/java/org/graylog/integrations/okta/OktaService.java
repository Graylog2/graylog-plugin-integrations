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

import com.google.inject.Inject;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;

public class OktaService {

    @Inject
    public OktaService() {

    }

    public OktaResponse getSystemLogs(String domain, String apiKey) throws IOException {
        // TODO change client to OkHttpClientProvider & improve call
        String url = "https://" + domain + "/api/v1/logs?since=2017-10-01T00:00:00.000Z";
        String key = "SSWS " + apiKey;
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", key)
                .build();

        okhttp3.Response response = client.newCall(request).execute();
        return OktaResponse.create(response.body().string());
    }
}