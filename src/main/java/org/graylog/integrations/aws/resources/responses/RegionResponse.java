package org.graylog.integrations.aws.resources.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class RegionResponse {

    private static final String REGION_ID = "region_id";
    private static final String REGION_DESCRIPTION = "region_description";
    private static final String DISPLAY_VALUE = "display_value";

    // eu-west-2
    @JsonProperty(REGION_ID)
    public abstract String regionId();

    // EU (London)
    @JsonProperty(REGION_DESCRIPTION)
    public abstract String regionDescription();

    // The combination of both the name and description for display in the UI:
    // EU (London): eu-west-2
    @JsonProperty(DISPLAY_VALUE)
    public abstract String displayValue();

    public static RegionResponse create(@JsonProperty(REGION_ID) String regionId,
                                        @JsonProperty(REGION_DESCRIPTION) String regionDescription,
                                        @JsonProperty(DISPLAY_VALUE) String displayValue) {
        return new AutoValue_RegionResponse(regionId, regionDescription, displayValue);
    }
}