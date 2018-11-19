package org.graylog.integrations.inputs.paloalto;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.graylog.integrations.inputs.paloalto.types.FieldDescription;
import org.graylog.integrations.inputs.paloalto.types.MessageMapping;
import org.graylog.integrations.inputs.paloalto.types.PANFieldTemplate;
import org.graylog.integrations.inputs.paloalto.types.PANMessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PANTypeParser {

    private static final Logger LOG = LoggerFactory.getLogger(PANTypeParser.class);

    private final ImmutableMap<Integer, FieldDescription> mapping;
    private final PANMessageTemplate messageTemplate;

    public PANTypeParser(MessageMapping mapping, PANMessageTemplate messageTemplate) {

        this.mapping = mapping.getMapping();
        this.messageTemplate = messageTemplate;
    }

    public ImmutableMap<String, Object> parseFields(List<String> fields) {
        ImmutableMap.Builder<String, Object> x = new ImmutableMap.Builder<>();

        for (PANFieldTemplate field : messageTemplate.getFields()) {
            String rawValue = fields.get(field.getPosition());
            Object value;

            switch (field.getFieldType()) {
                case STRING:
                    // Handle quoted values.
                    if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
                        rawValue = rawValue.substring(1, rawValue.length() - 1);
                    }

                    value = rawValue;
                    break;
                case LONG:
                    if (!Strings.isNullOrEmpty(rawValue)) {
                        value = Long.valueOf(rawValue);
                    } else {
                        value = 0L;
                    }
                    break;
                case BOOLEAN:
                    if (!Strings.isNullOrEmpty(rawValue)) {
                        value = Boolean.valueOf(rawValue);
                    } else {
                        value = false;
                    }
                    break;
                default:
                    throw new RuntimeException("Unhandled PAN mapping field type [" + field.getFieldType() + "].");
            }

            x.put(field.getField(), value);
        }
// TODO: Remove after template processing logic is finalized and tested.
//        for (Map.Entry<Integer, FieldDescription> map : mapping.entrySet()) {
//            String key = map.getValue().name();
//            String rawValue = fields.get(map.getKey());
//            Object value;
//
//            switch (map.getValue().type()) {
//                case STRING:
//                    // Handle quoted values.
//                    if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
//                        rawValue = rawValue.substring(1, rawValue.length() - 1);
//                    }
//
//                    value = rawValue;
//                    break;
//                case LONG:
//                    if (!Strings.isNullOrEmpty(rawValue)) {
//                        value = Long.valueOf(rawValue);
//                    } else {
//                        value = 0L;
//                    }
//                    break;
//                case BOOLEAN:
//                    if (!Strings.isNullOrEmpty(rawValue)) {
//                        value = Boolean.valueOf(rawValue);
//                    } else {
//                        value = false;
//                    }
//                    break;
//                default:
//                    throw new RuntimeException("Unhandled PAN mapping field type [" + map.getValue().type() + "].");
//            }
//
//            x.put(key, value);
//        }

        return x.build();
    }
}