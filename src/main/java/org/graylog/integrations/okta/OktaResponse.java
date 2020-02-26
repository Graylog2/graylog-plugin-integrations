package org.graylog.integrations.okta;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class OktaResponse {

    private static final String SYSTEM_LOGS = "systemLogs";

    @JsonProperty(SYSTEM_LOGS)
    public abstract String logs();

    // TODO auto create a okta system log event object https://developer.okta.com/docs/reference/api/system-log/#logevent-object
    public static OktaResponse create(@JsonProperty(SYSTEM_LOGS) String systemLogs) {
        return new AutoValue_OktaResponse(systemLogs);
    }
}