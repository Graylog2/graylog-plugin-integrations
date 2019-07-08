package org.graylog.integrations.aws;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import java.util.List;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class AWSPolicyStatement {

    private static final String SID = "Sid";
    private static final String EFFECT = "Effect";
    private static final String ACTION = "Action";
    private static final String RESOURCE = "Resource";

    @JsonProperty(SID)
    public abstract String sid();

    @JsonProperty(EFFECT)
    public abstract String effect();

    @JsonProperty(ACTION)
    public abstract List<String> action();

    @JsonProperty(RESOURCE)
    public abstract String resource();

    public static AWSPolicyStatement create(@JsonProperty(SID) String sid,
                                            @JsonProperty(EFFECT)String effect,
                                            @JsonProperty(ACTION)List<String> action,
                                            @JsonProperty(RESOURCE) String resource) {
        return new AutoValue_AWSPolicyStatement(sid, effect, action, resource);
    }
}