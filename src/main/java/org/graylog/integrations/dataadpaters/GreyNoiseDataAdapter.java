/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package org.graylog.integrations.dataadpaters;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.inject.assistedinject.Assisted;
import com.unboundid.util.json.JSONException;
import com.unboundid.util.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.graylog2.plugin.lookup.LookupCachePurge;
import org.graylog2.plugin.lookup.LookupDataAdapter;
import org.graylog2.plugin.lookup.LookupDataAdapterConfiguration;
import org.graylog2.plugin.lookup.LookupResult;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GreyNoiseDataAdapter extends LookupDataAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(GreyNoiseDataAdapter.class);
    public static final String NAME = "GreyNoise";

    OkHttpClient okHttpClient;
    Config config;

    @Inject
    public GreyNoiseDataAdapter(@Assisted("id") String id,
                                @Assisted("name") String name,
                                @Assisted LookupDataAdapterConfiguration config,
                                MetricRegistry metricRegistry) {
        super(id, name, config, metricRegistry);
        this.config = (Config) config;
    }

    @Override
    public void doStart() throws Exception {

        okHttpClient = new OkHttpClient().newBuilder()
                                         .build();

    }

    @Override
    public void doStop() throws Exception {

    }

    @Override
    public Duration refreshInterval() {
        return Duration.ZERO;
    }

    @Override
    protected void doRefresh(LookupCachePurge cachePurge) throws Exception {

    }

    @Override
    protected LookupResult doGet(Object keyObject) {
        try {
            Request request = new Request.Builder()
                    .url("https://api.greynoise.io/v2/noise/quick/" + keyObject.toString())
                    .method("GET", null)
                    .addHeader("Accept", "application/json")
                    .addHeader("key", config.apiToken())
                    .build();

            return parseResponse(okHttpClient.newCall(request).execute());

        } catch (Exception e) {
            LOG.error("An error occurred while retrieving lookup result [{}]", e.toString());
            return LookupResult.withError();
        }
    }

    public static LookupResult parseResponse(Response response) throws IOException {

        if (response.isSuccessful()) {
            Map<Object, Object> map = new HashMap<>();

            BufferedReader in = new BufferedReader(new InputStreamReader(Objects.requireNonNull(response.body()).byteStream()));
            String jsonString = in.readLine();
            try {
                JSONObject obj = new JSONObject(jsonString);
                map.put("ip", Objects.requireNonNull(obj).getFieldAsString("ip"));
                map.put("noise", Objects.requireNonNull(obj).getFieldAsBoolean("noise"));
                map.put("code", Objects.requireNonNull(obj).getFieldAsString("code"));
            } catch (JSONException e) {
                LOG.error("An error occurred while parsing Lookup result [{}]", e.toString());
            }
            return LookupResult.withoutTTL().multiValue(map)
                               .build();
        } else
            return LookupResult.empty();
    }

    @Override
    public void set(Object key, Object value) {
    }

    public interface Factory extends LookupDataAdapter.Factory<GreyNoiseDataAdapter> {

        @Override
        GreyNoiseDataAdapter create(@Assisted("id") String id,
                                    @Assisted("name") String name,
                                    LookupDataAdapterConfiguration configuration);

        @Override
        Descriptor getDescriptor();

    }

    public static class Descriptor extends LookupDataAdapter.Descriptor<Config> {

        public Descriptor() {
            super(NAME, Config.class);
        }

        @Override
        public Config defaultConfiguration() {
            return Config.builder()
                         .type(NAME)
                         .apiToken("token")
                         .build();
        }

    }

    @AutoValue
    @JsonAutoDetect
    @JsonDeserialize(builder = GreyNoiseDataAdapter.Config.Builder.class)
    @JsonTypeName(NAME)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static abstract class Config implements LookupDataAdapterConfiguration {

        @Override
        @JsonProperty(TYPE_FIELD)
        public abstract String type();

        @JsonProperty("api_token")
        @NotEmpty
        public abstract String apiToken();

        public static Builder builder() {
            return new AutoValue_GreyNoiseDataAdapter_Config.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            @JsonCreator
            public static Builder create() {
                return Config.builder();
            }

            @JsonProperty(TYPE_FIELD)
            public abstract Builder type(String type);

            // TODO resolve string key
            @JsonProperty("api_token")
            public abstract Builder apiToken(String user_passwd);

            public abstract Config build();
        }
    }
}
