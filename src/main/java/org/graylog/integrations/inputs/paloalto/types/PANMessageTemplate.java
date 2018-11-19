package org.graylog.integrations.inputs.paloalto.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * An object representation of a PAN message template. Defines which fields to pick out from the PAN
 * message at a particular position.
 *
 * This was made configurable to allow for user-selected fields and support for old/newer versions
 * without a software change.
 *
 * @see <a href="http://google.com">https://www.paloaltonetworks.com/documentation/80/pan-os/pan-os/monitoring/use-syslog-for-monitoring/syslog-field-descriptions/threat-log-fields</a>
 */

public class PANMessageTemplate {

    public static final String FIELDS = "fields";

    @JsonProperty(FIELDS)
    Set<PANFieldTemplate> fields;

    public Set<PANFieldTemplate> getFields() {
        return fields;
    }

    public void setFields(Set<PANFieldTemplate> fields) {
        this.fields = fields;
    }
}
