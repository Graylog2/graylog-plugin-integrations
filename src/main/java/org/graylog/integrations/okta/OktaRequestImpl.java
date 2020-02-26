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