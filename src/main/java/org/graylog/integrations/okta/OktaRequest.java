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