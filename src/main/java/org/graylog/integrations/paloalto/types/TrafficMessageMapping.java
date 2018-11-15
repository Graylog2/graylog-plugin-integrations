package org.graylog.integrations.paloalto.types;

import com.google.common.collect.ImmutableMap;

public class TrafficMessageMapping implements MessageMapping {

    public static final ImmutableMap<Integer, FieldDescription> MAP = new ImmutableMap.Builder<Integer, FieldDescription>()
            .put(1, FieldDescription.create("pa_time_received", FieldDescription.FIELD_TYPE.STRING))
            .put(2, FieldDescription.create("serial_number", FieldDescription.FIELD_TYPE.STRING))
            .put(3, FieldDescription.create("pa_type", FieldDescription.FIELD_TYPE.STRING))
            .put(4, FieldDescription.create("content_type", FieldDescription.FIELD_TYPE.STRING))
            .put(6, FieldDescription.create("pa_time_generated", FieldDescription.FIELD_TYPE.STRING))
            .put(7, FieldDescription.create("src_ip", FieldDescription.FIELD_TYPE.STRING))
            .put(8, FieldDescription.create("dest_ip", FieldDescription.FIELD_TYPE.STRING))
            .put(9, FieldDescription.create("nat_src_ip", FieldDescription.FIELD_TYPE.STRING))
            .put(10, FieldDescription.create("nat_dest_ip", FieldDescription.FIELD_TYPE.STRING))
            .put(11, FieldDescription.create("rule_name", FieldDescription.FIELD_TYPE.STRING))
            .put(12, FieldDescription.create("src_user", FieldDescription.FIELD_TYPE.STRING))
            .put(13, FieldDescription.create("dest_user", FieldDescription.FIELD_TYPE.STRING))
            .put(14, FieldDescription.create("application", FieldDescription.FIELD_TYPE.STRING))
            .put(15, FieldDescription.create("pa_virtualsys_name", FieldDescription.FIELD_TYPE.STRING))
            .put(16, FieldDescription.create("src_zone", FieldDescription.FIELD_TYPE.STRING))
            .put(17, FieldDescription.create("dst_zone", FieldDescription.FIELD_TYPE.STRING))
            .put(18, FieldDescription.create("interface_inbound", FieldDescription.FIELD_TYPE.STRING))
            .put(19, FieldDescription.create("interface_outbound", FieldDescription.FIELD_TYPE.STRING))
            .put(20, FieldDescription.create("log_action", FieldDescription.FIELD_TYPE.STRING))
            .put(22, FieldDescription.create("session_id", FieldDescription.FIELD_TYPE.LONG))
            .put(23, FieldDescription.create("repeat_count", FieldDescription.FIELD_TYPE.LONG))
            .put(24, FieldDescription.create("src_port", FieldDescription.FIELD_TYPE.LONG))
            .put(25, FieldDescription.create("dest_port", FieldDescription.FIELD_TYPE.LONG))
            .put(26, FieldDescription.create("nat_src_port", FieldDescription.FIELD_TYPE.LONG))
            .put(27, FieldDescription.create("nat_dest_port", FieldDescription.FIELD_TYPE.LONG))
            .put(29, FieldDescription.create("protocol", FieldDescription.FIELD_TYPE.STRING))
            .put(30, FieldDescription.create("action", FieldDescription.FIELD_TYPE.STRING))
            .put(31, FieldDescription.create("bytes", FieldDescription.FIELD_TYPE.LONG))
            .put(32, FieldDescription.create("bytes_sent", FieldDescription.FIELD_TYPE.LONG))
            .put(33, FieldDescription.create("bytes_received", FieldDescription.FIELD_TYPE.LONG))
            .put(34, FieldDescription.create("packets", FieldDescription.FIELD_TYPE.LONG))
            .put(37, FieldDescription.create("category", FieldDescription.FIELD_TYPE.STRING))
            .put(41, FieldDescription.create("src_location", FieldDescription.FIELD_TYPE.STRING))
            .put(42, FieldDescription.create("dest_location", FieldDescription.FIELD_TYPE.STRING))
            .put(44, FieldDescription.create("packets_sent", FieldDescription.FIELD_TYPE.LONG))
            .put(45, FieldDescription.create("packets_received", FieldDescription.FIELD_TYPE.LONG))
            .put(51, FieldDescription.create("pa_devicename", FieldDescription.FIELD_TYPE.STRING))
            .build();

    @Override
    public ImmutableMap<Integer, FieldDescription> getMapping() {
        return MAP;
    }

}
