package org.graylog.integrations.okta;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import java.util.List;

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class OktaResponse {

    private static final String SYSTEM_LOGS = "systemLogs";
    private static final String TOTAL = "total";

    @JsonProperty(SYSTEM_LOGS)
    public abstract List<String> logs();

    @JsonProperty(TOTAL)
    public abstract long total();

    // TODO auto create a okta system log event object https://developer.okta.com/docs/reference/api/system-log/#logevent-object
    public static OktaResponse create(@JsonProperty(SYSTEM_LOGS) List<String> systemLogs,
                                      @JsonProperty(TOTAL) long total) {
        return new AutoValue_OktaResponse(systemLogs, total);
    }
}