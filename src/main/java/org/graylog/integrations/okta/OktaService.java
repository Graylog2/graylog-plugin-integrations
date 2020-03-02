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

    public OktaResponse getSystemLogs(String domain, String apiKey, String since, String until, String after,
                                      String filter, String q, String sortOrder, int limit) throws IOException {
        String url = buildQuery(domain, since, until, after, filter, q, sortOrder, limit);

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

    public String buildQuery(String domain, String since, String until, String after, String filter, String q, String sortOrder, int limit) {

        String query = "?";
        if(since!=null){
            query+= "since=" + since;
        }
        if(until!=null){
            query+="&until=" + until;
        }
        if(after!=null){
            query+="&after=" + after;
        }
        if(filter!=null){
            query+="&filter=" + filter;
        }
        if(q!=null){
            query+="&q=" + q;
        }
        if(sortOrder!=null){
            query+="&sortOrder=" + sortOrder;
        }
        query+= "&limit=" + limit;
        return "https://" + domain + "/api/v1/logs" + query;
    }
}