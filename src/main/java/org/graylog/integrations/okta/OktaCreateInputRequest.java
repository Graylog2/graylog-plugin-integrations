/**
 * This file is part of Graylog.
 * <p>
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.integrations.okta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@AutoValue
@WithBeanGetter
@JsonDeserialize(builder = OktaCreateInputRequest.Builder.class)
public abstract class OktaCreateInputRequest implements OktaRequest {

    private static final String OKTA_NAME = "name";
    private static final String OKTA_MESSAGE_TYPE = "okta_input_type";
    private static final String BATCH_SIZE = "batch_size";
    private static final String GLOBAL = "global";
    private static final String THROTTLING_ALLOWED = "enable_throttling";
    private static final String ADD_SYSTEM_LOG_PREFIX = "add_system_log_prefix";

    @JsonProperty(OKTA_NAME)
    public abstract String name();

    @JsonProperty(OKTA_MESSAGE_TYPE)
    public abstract String oktaMessageType();

    @JsonProperty(BATCH_SIZE)
    public abstract int batchSize();

    @JsonProperty(GLOBAL)
    public abstract boolean global();

    @JsonProperty(THROTTLING_ALLOWED)
    public abstract boolean throttlingAllowed();

    @JsonProperty(ADD_SYSTEM_LOG_PREFIX)
    public abstract boolean addFlowLogPrefix();

    public static Builder builder() {
        return Builder.create();
    }

    @AutoValue.Builder
    public static abstract class Builder implements OktaRequest.Builder<Builder> {

        public static Builder create() {
            return new Builder() {
                @Override
                public Builder name(String name) {
                    return null;
                }

                @Override
                public Builder oktaDomain(String oktaDomain) {
                    return null;
                }

                @Override
                public Builder oktaApiKey(String oktaApiKey) {
                    return null;
                }

                @Override
                public Builder oktaMessageType(String oktaMessageType) {
                    return null;
                }

                @Override
                public Builder batchSize(int batchSize) {
                    return null;
                }

                @Override
                public Builder global(boolean global) {
                    return null;
                }

                @Override
                public Builder throttlingAllowed(boolean throttlingAllowed) {
                    return null;
                }

                @Override
                public Builder addFlowLogPrefix(boolean addFlowLogPrefix) {
                    return null;
                }

                @Override
                public OktaCreateInputRequest builder() {
                    return null;
                }
            };
        }

        @JsonProperty(OKTA_NAME)
        public abstract Builder name(String name);

        @JsonProperty(OKTA_MESSAGE_TYPE)
        public abstract Builder oktaMessageType(String oktaMessageType);

        @JsonProperty(BATCH_SIZE)
        public abstract Builder batchSize(int batchSize);

        @JsonProperty(GLOBAL)
        public abstract Builder global(boolean global);

        @JsonProperty(THROTTLING_ALLOWED)
        public abstract Builder throttlingAllowed(boolean throttlingAllowed);

        @JsonProperty(ADD_SYSTEM_LOG_PREFIX)
        public abstract Builder addFlowLogPrefix(boolean addFlowLogPrefix);

        public abstract OktaCreateInputRequest builder();
    }
}