package org.graylog.integrations.inputs.paloalto;

public class PaloAltoFieldTemplate {


    private Integer position;

    private String field;

    private PaloAltoFieldType fieldType;

    public PaloAltoFieldTemplate(String field, Integer position, PaloAltoFieldType fieldType) {
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

    public PaloAltoFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(PaloAltoFieldType fieldType) {
        this.fieldType = fieldType;
    }
}
