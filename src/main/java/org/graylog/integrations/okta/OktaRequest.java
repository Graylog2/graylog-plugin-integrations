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
    String oktaDomain();

    @Nullable
    @JsonProperty(OKTA_API_KEY)
    String oktaApiKey();


    interface Builder<SELF> {

        @JsonProperty(OKTA_NAME)
        SELF name(String okta);

        @JsonProperty(OKTA_DOMAIN)
        SELF oktaDomain(String oktaDomain);

        @JsonProperty(OKTA_API_KEY)
        SELF oktaApiKey(String oktaApiKey);

    }
}