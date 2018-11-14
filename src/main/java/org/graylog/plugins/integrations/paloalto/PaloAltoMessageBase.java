package org.graylog.plugins.integrations.paloalto;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;

@AutoValue
public abstract class PaloAltoMessageBase {

    public abstract String source();
    public abstract DateTime timestamp();
    public abstract String payload();
    public abstract String panType();
    public abstract ImmutableList<String> fields();

    public static PaloAltoMessageBase create(String source, DateTime timestamp, String payload, String panType, ImmutableList<String> fields) {
        return builder()
                .source(source)
                .timestamp(timestamp)
                .payload(payload)
                .panType(panType)
                .fields(fields)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PaloAltoMessageBase.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder source(String source);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder payload(String payload);

        public abstract Builder panType(String panType);

        public abstract Builder fields(ImmutableList<String> fields);

        public abstract PaloAltoMessageBase build();
    }

}
