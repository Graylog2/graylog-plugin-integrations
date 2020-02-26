package org.graylog.integrations.okta;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

public interface OktaRequest {

    String OKTA_NAME = "name";
    String OKTA_DOMAIN = "okta_domain";
    String OKTA_API_KEY = "okta_api_key";

    @Nullable
    @JsonProperty(OKTA_NAME)
    String name();

    @Nullable
    @JsonProperty(OKTA_DOMAIN)
    String domain();

    @Nullable
    @JsonProperty(OKTA_API_KEY)
    String apiKey();

    interface Builder<SELF> {

        @JsonProperty(OKTA_NAME)
        SELF name(String okta);

        @JsonProperty(OKTA_DOMAIN)
        SELF domain(String domain);

        @JsonProperty(OKTA_API_KEY)
        SELF apiKey(String apiKey);
    }
}