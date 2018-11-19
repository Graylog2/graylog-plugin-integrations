package org.graylog.integrations.inputs.paloalto.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PANFieldTemplate {

    private static final String POSITION = "position";
    private static final String FIELD = "field";
    private static final String TYPE = "type";

    @JsonProperty(POSITION)
    private Integer position;

    @JsonProperty(FIELD)
    private String field;

    @JsonProperty(TYPE)
    private FieldDescription.FIELD_TYPE fieldType;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public FieldDescription.FIELD_TYPE getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldDescription.FIELD_TYPE fieldType) {
        this.fieldType = fieldType;
    }
}
