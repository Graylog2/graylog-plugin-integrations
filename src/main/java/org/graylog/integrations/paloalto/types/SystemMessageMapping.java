package org.graylog.integrations.paloalto.types;

import com.google.common.collect.ImmutableMap;

public class SystemMessageMapping implements MessageMapping {

    public static final ImmutableMap<Integer, FieldDescription> MAP = new ImmutableMap.Builder<Integer, FieldDescription>()
            .put(1, FieldDescription.create("pa_time_received", FieldDescription.FIELD_TYPE.STRING))
            .put(2, FieldDescription.create("serial_number", FieldDescription.FIELD_TYPE.STRING))
            .put(3, FieldDescription.create("pa_type", FieldDescription.FIELD_TYPE.STRING))
            .put(4, FieldDescription.create("content_type", FieldDescription.FIELD_TYPE.STRING))
            .put(6, FieldDescription.create("pa_time_generated", FieldDescription.FIELD_TYPE.STRING))
            .put(7, FieldDescription.create("virtual_system", FieldDescription.FIELD_TYPE.STRING))
            .put(8, FieldDescription.create("event_id", FieldDescription.FIELD_TYPE.STRING))
            .put(9, FieldDescription.create("object", FieldDescription.FIELD_TYPE.STRING))
            .put(12, FieldDescription.create("module", FieldDescription.FIELD_TYPE.STRING))
            .put(13, FieldDescription.create("pa_severity", FieldDescription.FIELD_TYPE.STRING))
            .put(14, FieldDescription.create("description", FieldDescription.FIELD_TYPE.STRING))
            .put(21, FieldDescription.create("pa_virtualsys_name", FieldDescription.FIELD_TYPE.STRING))
            .put(22, FieldDescription.create("pa_devicename", FieldDescription.FIELD_TYPE.STRING))
            .build();

    @Override
    public ImmutableMap<Integer, FieldDescription> getMapping() {
        return MAP;
    }
}
