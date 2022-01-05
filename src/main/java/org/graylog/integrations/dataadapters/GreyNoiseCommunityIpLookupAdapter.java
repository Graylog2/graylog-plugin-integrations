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
package org.graylog.integrations.dataadapters;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.assistedinject.Assisted;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.graylog2.plugin.Version;
import org.graylog2.plugin.lookup.LookupCachePurge;
import org.graylog2.plugin.lookup.LookupDataAdapter;
import org.graylog2.plugin.lookup.LookupDataAdapterConfiguration;
import org.graylog2.plugin.lookup.LookupResult;
import org.graylog2.security.encryption.EncryptedValue;
import org.graylog2.security.encryption.EncryptedValueService;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * A {@link LookupDataAdapter} that uses the <a href="https://docs.greynoise.io/reference/get_v3-community-ip">GreyNoise Community API</a>
 * to perform IP lookups.
 *
 * <p>
 * The API response is a subset of the IP context returned by the full IP Lookup API.
 * </p>
 */
public class GreyNoiseCommunityIpLookupAdapter extends LookupDataAdapter {

    public static final String ADAPTER_NAME = "GreyNoise Community IP Lookup";

    protected static final String GREYNOISE_COMMUNITY_ENDPOINT = "https://api.greynoise.io/v3/community";

    private static final Logger LOG = LoggerFactory.getLogger(GreyNoiseCommunityIpLookupAdapter.class);
    private static final String USER_AGENT = "Graylog/%s";
    private static final String ACCEPT_TYPE = "application/json";
    private static final String METHOD = "GET";

    private final EncryptedValueService encryptedValueService;
    private final Config config;
    private final OkHttpClient okHttpClient;

    @Inject
    public GreyNoiseCommunityIpLookupAdapter(@Assisted("id") String id,
                                             @Assisted("name") String name,
                                             @Assisted LookupDataAdapterConfiguration config,
                                             MetricRegistry metricRegistry,
                                             EncryptedValueService encryptedValueService,
                                             OkHttpClient okHttpClient) {
        super(id, name, config, metricRegistry);
        this.config = (GreyNoiseCommunityIpLookupAdapter.Config) config;
        this.encryptedValueService = encryptedValueService;
        this.okHttpClient = okHttpClient;
    }

    @Override
    protected void doStart(){
        //Not needed
    }

    @Override
    protected void doStop() {
        //Not needed
    }

    @Override
    public Duration refreshInterval() {
        return Duration.ZERO;
    }

    @Override
    protected void doRefresh(LookupCachePurge cachePurge) {
        //Not needed
    }

    @Override
    public void set(Object key, Object value) {
        //Not needed
    }

    @Override
    protected LookupResult doGet(Object ipAddress) {

        Request request = createRequest(ipAddress);

        LookupResult result;
        try (Response response = okHttpClient.newCall(request).execute()) {
            result = parseResponse(response);

        } catch (IOException e) {
            LOG.error("an error occurred while retrieving GreyNoise IP data. {}", e.getMessage(), e);
            result = LookupResult.withError();
        }
        return result;
    }

    private Request createRequest(Object ipAddress) {
        String ipString = ipAddress == null? "" : ipAddress.toString();
        if(ipString.trim().isEmpty()){
            String error = String.format("[%s] requires an IP address to perform Lookup",ADAPTER_NAME);
            throw new IllegalArgumentException(error);
        }

        String userAgent = String.format(USER_AGENT, Version.CURRENT_CLASSPATH);
        String apiToken = encryptedValueService.decrypt(config.apiToken());
        if(apiToken == null || apiToken.trim().isEmpty()){
            String error = String.format("[%s] requires a non-null API Token",ADAPTER_NAME);
            throw new IllegalArgumentException(error);
        }

        return new Request.Builder()
                .url(String.join("/", GREYNOISE_COMMUNITY_ENDPOINT, ipString))
                .method(METHOD, null)
                .addHeader("Accept", ACCEPT_TYPE)
                .addHeader("User-Agent", userAgent)
                .addHeader("key", apiToken)
                .build();
    }

    @VisibleForTesting
    static LookupResult parseResponse(Response response) {

        final LookupResult result;
        if (response.isSuccessful()) {
            result = createSuccessfulResult(response);
        } else {
            result = createUnsuccessfulResult(response);
        }

        return result;
    }

    private static LookupResult createUnsuccessfulResult(Response response) {

        Map<Object, Object> allValues = getResponseValueMap(response);

        //a 404 indicates the input IP does not exist
        //but the call as such was successful and thus no actual error has occurred.
        boolean hasError = response.code() != 404;

        return LookupResult.withoutTTL()
                .multiValue(allValues)
                .hasError(hasError)
                .build();
    }

    private static LookupResult createSuccessfulResult(Response response) {

        Map<Object, Object> allValues = getResponseValueMap(response);

        return LookupResult.withoutTTL()
                .multiValue(allValues)
                .build();
    }

    /**
     * Load the {@link Response#body()} fields into a {@link Map}.
     *
     * @param response response
     * @return map of fields
     */
    private static Map<Object, Object> getResponseValueMap(Response response) {
        Map<Object, Object> values;
        try {
            if (response.body() == null) {
                values = Collections.emptyMap();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                TypeReference<Map<Object, Object>> ref = new TypeReference<Map<Object, Object>>() {};
                values = mapper.readValue(response.body().byteStream(), ref);
            }

        } catch (IOException e) {
            LOG.error("An error occurred while parsing parsing Lookup result. {}", e.getMessage(), e);
            values = Collections.emptyMap();
        }
        return values;
    }

    public interface Factory extends LookupDataAdapter.Factory<GreyNoiseCommunityIpLookupAdapter> {

        @Override
        GreyNoiseCommunityIpLookupAdapter create(@Assisted("id") String id,
                                                 @Assisted("name") String name,
                                                 LookupDataAdapterConfiguration configuration);

        @Override
        Descriptor getDescriptor();

    }

    public static class Descriptor extends LookupDataAdapter.Descriptor<GreyNoiseCommunityIpLookupAdapter.Config> {

        public Descriptor() {
            super(ADAPTER_NAME, GreyNoiseCommunityIpLookupAdapter.Config.class);
        }

        @Override
        public GreyNoiseCommunityIpLookupAdapter.Config defaultConfiguration() {
            return GreyNoiseCommunityIpLookupAdapter.Config.builder()
                    .type(ADAPTER_NAME)
                    .apiToken(EncryptedValue.createUnset())
                    .build();
        }
    }

    @AutoValue
    @JsonAutoDetect
    @JsonDeserialize(builder = GreyNoiseCommunityIpLookupAdapter.Config.Builder.class)
    @JsonTypeName(ADAPTER_NAME)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public abstract static class Config implements LookupDataAdapterConfiguration {

        @JsonProperty("api_token")
        @NotEmpty
        public abstract EncryptedValue apiToken();

        public static GreyNoiseCommunityIpLookupAdapter.Config.Builder builder() {
            return new AutoValue_GreyNoiseCommunityIpLookupAdapter_Config.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            @JsonCreator
            public static Builder create() {
                return builder();
            }

            @JsonProperty(TYPE_FIELD)
            public abstract Builder type(String type);

            @JsonProperty("api_token")
            public abstract Builder apiToken(EncryptedValue apiToken);

            public abstract GreyNoiseCommunityIpLookupAdapter.Config build();
        }
    }
}