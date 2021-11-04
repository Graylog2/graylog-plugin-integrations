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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link LookupDataAdapter} use the <a href="https://docs.greynoise.io/reference/get_v3-community-ip">GreyNoise Community API</a>
 * to perform IP lookups.
 *
 * <p>
 *     The API response is a subset of the IP context returned by the IP Lookup API.
 * </p>
 */
public class CommunityGreyNoiseIpLookupAdapter extends LookupDataAdapter {

    public static final String ADAPTER_NAME="GreyNoise Community IP Lookup";

    protected static final String GREYNOISE_ENDPOINT="https://api.greynoise.io/v3/community";

    private static final Logger LOG =  LoggerFactory.getLogger(CommunityGreyNoiseIpLookupAdapter.class);
    private static final String USER_AGENT="Graylog/%s";
    private static final String ACCEPT_TYPE="application/json";
    private static final String METHOD="GET";

    private final EncryptedValueService encryptedValueService;
    private final Config config;
    private final OkHttpClient okHttpClient;

    @Inject
    public CommunityGreyNoiseIpLookupAdapter(@Assisted("id") String id,
                                @Assisted("name") String name,
                                @Assisted LookupDataAdapterConfiguration config,
                                MetricRegistry metricRegistry,
                                EncryptedValueService encryptedValueService,
                                OkHttpClient okHttpClient) {
        super(id, name, config, metricRegistry);
        this.config = (CommunityGreyNoiseIpLookupAdapter.Config) config;
        this.encryptedValueService = encryptedValueService;
        this.okHttpClient = okHttpClient;
    }

    @Override
    protected void doStart() throws Exception {
        //Not needed
    }

    @Override
    protected void doStop() throws Exception {
        //Not needed
    }

    @Override
    public Duration refreshInterval() {
        return null;
    }

    @Override
    protected void doRefresh(LookupCachePurge cachePurge) throws Exception {
        //Not needed
    }

    @Override
    public void set(Object key, Object value) {
        //Not needed
    }

    @Override
    protected LookupResult doGet(Object ipAddress) {

        String ipString = Objects.requireNonNull(ipAddress).toString();
        String userAgent = String.format(USER_AGENT, Version.CURRENT_CLASSPATH);
        String apiToken = Objects.requireNonNull(encryptedValueService.decrypt(config.apiToken()));
        Request request = new Request.Builder()
                .url(String.join("/",GREYNOISE_ENDPOINT,ipString))
                .method(METHOD,null)
                .addHeader("Accept",ACCEPT_TYPE)
                .addHeader("key",apiToken)
                .addHeader("User-Agent",userAgent)
                .build();

        LookupResult result;
        try(Response response = okHttpClient.newCall(request).execute()){
            result = parseResponse(response);

        }catch (IOException e){
            LOG.error("an error occurred while retrieving GreyNoise IP data. {}",e.getMessage(),e);
            result = LookupResult.withError();
        }
        return result;
    }

    @VisibleForTesting
    static LookupResult parseResponse(Response response) {

        final LookupResult result;
        if(response.isSuccessful()){

            result = createSuccessfulResult(response);
        }else{
            result = createUnsuccessfulResult(response);
        }

        return result;
    }

    private static LookupResult createUnsuccessfulResult(Response response) {
        final LookupResult result;
        Map<Object, Object> resultValues = new HashMap<>();
        Map<Object, Object> allValues = getResponseValueMap(response);
        resultValues.put("message",allValues.get("message"));
        boolean hasError;
        switch(response.code())
        {
            case 404:
                resultValues.put("ip",allValues.get("ip"));
                resultValues.put("noise",allValues.get("noise"));
                resultValues.put("riot",allValues.get("riot"));
                hasError = false;
                break;
            case 429:
                resultValues.put("plan",allValues.get("plan"));
                resultValues.put("rate-limit",allValues.get("rate-limit"));
                resultValues.put("plan_url",allValues.get("plan_url"));
                hasError = true;
                break;
            default:
                hasError = true;
        }

        result = LookupResult.withoutTTL()
                .multiValue(resultValues)
                .hasError(hasError)
                .build();
        return result;
    }

    private static LookupResult createSuccessfulResult(Response response) {

        Map<Object, Object> resultValues = new HashMap<>();
        Map<Object, Object> allValues = getResponseValueMap(response);
        resultValues.put("ip", allValues.get("ip"));
        resultValues.put("noise",allValues.get("noise"));
        resultValues.put("riot",allValues.get("riot"));
        resultValues.put("classification",allValues.get("classification"));
        resultValues.put("name",allValues.get("name"));
        resultValues.put("link",allValues.get("link"));
        resultValues.put("last_seen",allValues.get("last_seen"));
        resultValues.put("message",allValues.get("message"));

        return LookupResult.withoutTTL()
                .multiValue(resultValues)
                .build();
    }

    private static Map<Object,Object> getResponseValueMap(Response response){
        Map<Object,Object> values;
        try{
            if(response.body() == null) {
                values = Collections.emptyMap();
            }
            else {
                ObjectMapper mapper = new ObjectMapper();
                TypeReference<Map<Object,Object>> ref = new TypeReference<Map<Object, Object>>() {};
                values = mapper.readValue(response.body().byteStream(),ref);
            }

        }catch(IOException e){
            LOG.error("An error occurred while parsing parsing Lookup result. {}",e.getMessage(),e);
            values = Collections.emptyMap();
        }
        return values;
    }

    public interface Factory extends LookupDataAdapter.Factory<CommunityGreyNoiseIpLookupAdapter> {

        @Override
        CommunityGreyNoiseIpLookupAdapter create(@Assisted("id") String id,
                                    @Assisted("name") String name,
                                    LookupDataAdapterConfiguration configuration);

        @Override
        Descriptor getDescriptor();

    }

    public static class Descriptor extends LookupDataAdapter.Descriptor<CommunityGreyNoiseIpLookupAdapter.Config> {

        public Descriptor() {
            super(ADAPTER_NAME, CommunityGreyNoiseIpLookupAdapter.Config.class);
        }

        @Override
        public CommunityGreyNoiseIpLookupAdapter.Config defaultConfiguration() {
            return CommunityGreyNoiseIpLookupAdapter.Config.builder()
                    .type(ADAPTER_NAME)
                    .apiToken(EncryptedValue.createUnset())
                    .build();
        }

    }

    @AutoValue
    @JsonAutoDetect
    @JsonDeserialize(builder = CommunityGreyNoiseIpLookupAdapter.Config.Builder.class)
    @JsonTypeName(ADAPTER_NAME)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public abstract static class Config implements LookupDataAdapterConfiguration {

        @JsonProperty("api_token")
        @NotEmpty
        public abstract EncryptedValue apiToken();

        public static CommunityGreyNoiseIpLookupAdapter.Config.Builder builder() {
            return new AutoValue_CommunityGreyNoiseIpLookupAdapter_Config.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            @JsonCreator
            public static CommunityGreyNoiseIpLookupAdapter.Config.Builder create() {
                return CommunityGreyNoiseIpLookupAdapter.Config.builder();
            }

            @JsonProperty(TYPE_FIELD)
            public abstract CommunityGreyNoiseIpLookupAdapter.Config.Builder type(String type);

            @JsonProperty("api_token")
            public abstract CommunityGreyNoiseIpLookupAdapter.Config.Builder apiToken(EncryptedValue apiToken);

            public abstract CommunityGreyNoiseIpLookupAdapter.Config build();
        }
    }
}
