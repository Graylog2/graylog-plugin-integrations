package org.graylog.integrations.dataadpaters;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
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
import java.io.IOException;

public class GreyNoiseDataAdapter extends LookupDataAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(GreyNoiseDataAdapter.class);
    public static final String NAME = "GreyNoise";

    OkHttpClient okHttpClient;
    Config config;

    private static final ImmutableSet<String> GreyNoiseResponse = ImmutableSet.<String>builder()
            .add("code")
            .add("ip")
            .add("seen")
            .build();

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
                    .url("https://api.greynoise.io/v2/noise/context/" + keyObject.toString())
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

            // TODO resolve parsing
            return LookupResult.withoutTTL().multiSingleton(response)
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
