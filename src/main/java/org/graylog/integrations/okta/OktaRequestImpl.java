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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@AutoValue
@WithBeanGetter
@JsonDeserialize(builder = OktaRequestImpl.Builder.class)
public abstract class OktaRequestImpl implements OktaRequest {

    public static Builder builder() {
        return Builder.create();
    }

    @AutoValue.Builder
    public static abstract class Builder implements OktaRequest.Builder<Builder> {
        @JsonCreator
        public static Builder create() {
            return new Builder() {
                @Override
                public Builder name(String okta) {
                    return null;
                }

                @Override
                public Builder domain(String domain) {
                    return null;
                }

                @Override
                public Builder apiKey(String apiKey) {
                    return null;
                }

                @Override
                public OktaRequestImpl build() {
                    return null;
                }
            };
        }
        public abstract OktaRequestImpl build();
    }
}