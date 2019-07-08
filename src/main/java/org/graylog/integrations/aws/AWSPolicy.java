package org.graylog.integrations.aws;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class AWSPolicy {

    private static final String VERSION = "Version";
    private static final String STATEMENT = "Statement";

    @JsonProperty(VERSION)
    public abstract String version();

    @JsonProperty(STATEMENT)
    public abstract AWSPolicyStatement statement();

    public static AWSPolicy create(@JsonProperty(VERSION) String version,
                                   @JsonProperty(STATEMENT) AWSPolicyStatement statement) {
        return new AutoValue_AWSPolicy(version, statement);
    }
}