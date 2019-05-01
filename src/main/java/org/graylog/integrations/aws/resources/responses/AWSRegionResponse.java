package org.graylog.integrations.aws.resources.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class AWSRegionResponse {

    // eu-west-2
    @JsonProperty
    public abstract String regionId();

    // EU (London)
    @JsonProperty
    public abstract String regionDescription();

    // The combination of both the name and description for display in the UI:
    // EU (London): eu-west-2
    @JsonProperty
    public abstract String displayValue();

    public static AWSRegionResponse create(@JsonProperty("regionId") String regionId,
                                           @JsonProperty("regionDescription") String regionDescription,
                                           @JsonProperty("displayValue") String displayValue ) {
        return new AutoValue_AWSRegionResponse(regionId, regionDescription, displayValue);
    }
}