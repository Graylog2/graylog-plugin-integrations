package org.graylog.integrations.paloalto;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.graylog.integrations.paloalto.types.FieldDescription;
import org.graylog.integrations.paloalto.types.MessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class PANTypeParser {

    private static final Logger LOG = LoggerFactory.getLogger(PANTypeParser.class);

    private final ImmutableMap<Integer, FieldDescription> mapping;

    public PANTypeParser(MessageMapping mapping) {
        this.mapping = mapping.getMapping();
    }

    public ImmutableMap<String, Object> parseFields(List<String> fields) {
        ImmutableMap.Builder<String, Object> x = new ImmutableMap.Builder<>();

        for (Map.Entry<Integer, FieldDescription> map : mapping.entrySet()) {
            String key = map.getValue().name();
            String rawValue = fields.get(map.getKey());
            Object value;

            switch (map.getValue().type()) {
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
                    throw new RuntimeException("Unhandled PAN mapping field type [" + map.getValue().type() + "].");
            }

            x.put(key, value);
        }

        return x.build();
    }
}