package org.graylog.integrations.inputs.paloalto.types;

public class PaloAltoFieldTemplate {


    private Integer position;

    private String field;

    private FieldDescription.FIELD_TYPE fieldType;

    public PaloAltoFieldTemplate(String field, Integer position, FieldDescription.FIELD_TYPE fieldType) {
        this.position = position;
        this.field = field;
        this.fieldType = fieldType;
    }

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
