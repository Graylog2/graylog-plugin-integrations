package org.graylog.plugins.integrations.paloalto.types;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FieldDescription {

    public enum FIELD_TYPE {
        STRING, LONG, BOOLEAN
    }

    public abstract String name();
    public abstract FIELD_TYPE type();

    public static FieldDescription create(String name, FIELD_TYPE type) {
        return builder()
                .name(name)
                .type(type)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_FieldDescription.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder type(FIELD_TYPE type);

        public abstract FieldDescription build();
    }

}
