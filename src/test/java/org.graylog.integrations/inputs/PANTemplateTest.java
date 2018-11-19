package org.graylog.integrations.inputs;

import org.graylog.integrations.inputs.paloalto.types.PANTemplateBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test parsing of raw PAN message templates.
 */
public class PANTemplateTest {

    @Test
    public void parseTest() throws Exception {

        PANTemplateBuilder builder = PANTemplateBuilder.newInstance(null, null, null);

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
}