package org.graylog.integrations.inputs.paloalto.types;

import com.google.common.collect.ImmutableMap;

public interface MessageMapping {

    ImmutableMap<Integer, FieldDescription> getMapping();

}
