package org.graylog.integrations.inputs;

import org.graylog.integrations.inputs.paloalto.PaloAltoTemplateDefaults;
import org.graylog.integrations.inputs.paloalto.PaloAltoTemplates;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test parsing of raw PAN message templates.
 */
public class PANTemplateTest {

    public static final String DEFAULT_HEADER = "field,position,type";

    @Test
    public void parseTest() throws Exception {

        PaloAltoTemplates builder = PaloAltoTemplates.newInstance(PaloAltoTemplateDefaults.SYSTEM_TEMPLATE,
                                                                  PaloAltoTemplateDefaults.THREAT_TEMPLATE,
                                                                  PaloAltoTemplateDefaults.TRAFFIC_TEMPLATE);

        // Verify that the correct number of fields were parsed.
        assertEquals(13, builder.getSystemMessageTemplate().getFields().size());
        assertEquals(73, builder.getThreatMessageTemplate().getFields().size());
        assertEquals(37, builder.getTrafficMessageTemplate().getFields().size());

        // Verify that all values are filled.
        builder.getSystemMessageTemplate().getFields().forEach(v -> {
            assertNotNull(v.getPosition());
            assertNotNull(v.getField());
            assertNotNull(v.getFieldType());
        });

        builder.getThreatMessageTemplate().getFields().forEach(v -> {
            assertNotNull(v.getPosition());
            assertNotNull(v.getField());
            assertNotNull(v.getFieldType());
        });

        builder.getTrafficMessageTemplate().getFields().forEach(v -> {
            assertNotNull(v.getPosition());
            assertNotNull(v.getField());
            assertNotNull(v.getFieldType());
        });
    }

    @Test
    public void verifyCSVValidation() {

        // Verify header checking.
        PaloAltoTemplates templates = PaloAltoTemplates.newInstance("badheader",
                                                                    DEFAULT_HEADER,
                                                                    DEFAULT_HEADER);
        assertEquals(3, templates.getAllErrors().size());
        templates.getAllErrors().forEach(error -> {
            assertTrue(error.contains("The header row is invalid"));
        });

        // Verify that invalid value messages returned for invalid values.
        templates = PaloAltoTemplates.newInstance("field,position,type\n" +
                                                  "badvalue",
                                                  DEFAULT_HEADER,
                                                  DEFAULT_HEADER);

        templates.getAllErrors().forEach(error -> {
            assertTrue(error.contains("[] is not a valid"));
        });
    }
}