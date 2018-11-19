package org.graylog.integrations.inputs.paloalto.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Builds PAN message templates.
 */
public class PANTemplateBuilder {

    private PANMessageTemplate systemMessageTemplate;
    private PANMessageTemplate threatMessageTemplate;
    private PANMessageTemplate trafficMessageTemplate;

    public static PANTemplateBuilder newInstance(String systemJson, String threatJson, String trafficJson) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        // Use default templates if no template supplied.
        PANTemplateBuilder builder = new PANTemplateBuilder();
        String systemTemplate = StringUtils.isNotBlank(systemJson) ? systemJson : PANTemplateDefaults.SYSTEM_TEMPLATE;
        String threatTemplate = StringUtils.isNotBlank(systemJson) ? threatJson : PANTemplateDefaults.THREAT_TEMPLATE;
        String trafficTemplate = StringUtils.isNotBlank(systemJson) ? trafficJson : PANTemplateDefaults.TRAFFIC_TEMPLATE;

        builder.systemMessageTemplate = objectMapper.readValue(systemTemplate, PANMessageTemplate.class);
        builder.threatMessageTemplate = objectMapper.readValue(threatTemplate, PANMessageTemplate.class);
        builder.trafficMessageTemplate = objectMapper.readValue(trafficTemplate, PANMessageTemplate.class);

        return builder;
    }

    public PANMessageTemplate getSystemMessageTemplate() {
        return systemMessageTemplate;
    }

    public PANMessageTemplate getThreatMessageTemplate() {
        return threatMessageTemplate;
    }

    public PANMessageTemplate getTrafficMessageTemplate() {
        return trafficMessageTemplate;
    }
}