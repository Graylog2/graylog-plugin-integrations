package org.graylog.integrations.inputs.paloalto.types;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.graylog2.plugin.inputs.MisfireException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.IntStream;

import static org.graylog.integrations.inputs.paloalto.types.FieldDescription.FIELD_TYPE.*;
import static org.graylog.integrations.inputs.paloalto.types.PANTemplateDefaults.*;

/**
 * Builds PAN message templates.
 */
public class PANTemplateBuilder {

    private PANMessageTemplate systemMessageTemplate;
    private PANMessageTemplate threatMessageTemplate;
    private PANMessageTemplate trafficMessageTemplate;

    private boolean builtSuccessfully = false;

    private static final Logger LOG = LoggerFactory.getLogger(PANTemplateBuilder.class);

    public static PANTemplateBuilder newInstance(String systemJson, String threatJson, String trafficJson) throws IOException {

        // Use default templates if no template supplied.
        PANTemplateBuilder builder = new PANTemplateBuilder();
        String systemTemplate = StringUtils.isNotBlank(systemJson) ? systemJson : SYSTEM_TEMPLATE;
        String threatTemplate = StringUtils.isNotBlank(systemJson) ? threatJson : THREAT_TEMPLATE;
        String trafficTemplate = StringUtils.isNotBlank(systemJson) ? trafficJson : TRAFFIC_TEMPLATE;

        builder.systemMessageTemplate = readCSV(systemTemplate, PANMessageType.SYSTEM);
        builder.threatMessageTemplate = readCSV(threatTemplate, PANMessageType.THREAT);
        builder.trafficMessageTemplate = readCSV(trafficTemplate, PANMessageType.TRAFFIC);

        return builder;
    }

    private static PANMessageTemplate readCSV(String CSV, PANMessageType messageType) throws IOException {

        Reader in = new StringReader(CSV);
        CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT);
        List<CSVRecord> list = parser.getRecords();

        PANMessageTemplate template = new PANMessageTemplate();


        // Periodically check errors to provide as much feedback to the user as possible about any misconfiguration.
        if (list.isEmpty()) {
            template.addError(String.format("The header row is missing. It must include the following fields: [%s,%s,%s]", POSITION, FIELD, TYPE));
        }

        if (template.hasErrors()) {
            return template;
        }

        if (!list.stream().findFirst().filter(row -> row.toString().contains(POSITION)).isPresent()) {
            template.addError(String.format("The header row is invalid. It must include the [%s] field", POSITION));
        }

        if (!list.stream().findFirst().filter(row -> row.toString().contains(FIELD)).isPresent()) {
            template.addError(String.format("The header row is invalid. It must include the [%s] field", FIELD));
        }

        if (!list.stream().findFirst().filter(row -> row.toString().contains(TYPE)).isPresent()) {
            template.addError(String.format("The header row is invalid. It must include the [%s] field", TYPE));
        }

        if (template.hasErrors()) {
            return template;
        }

        // Read header indexes.
        LOG.trace("Parsing CSV header.");

        // We've already verified that the first element exists.
        CSVRecord headerRow = list.get(0);

        // All indexes will be non-null, since we've already verify that they exist.
        int positionIndex = IntStream.range(0, headerRow.size())
                                     .filter(i -> POSITION.equals(headerRow.get(i)))
                                     .findFirst().getAsInt();

        int fieldIndex = IntStream.range(0, headerRow.size())
                                  .filter(i -> FIELD.equals(headerRow.get(i)))
                                  .findFirst().getAsInt();

        int typeIndex = IntStream.range(0, headerRow.size())
                                 .filter(i -> TYPE.equals(headerRow.get(i)))
                                 .findFirst().getAsInt();


        if (list.size() <= 1) {
            LOG.error(String.format("No fields were specified for the [%s] message type", messageType));
            return template;
        }

        // Skip header row.
        LOG.trace("Parsing CSV rows");
        list.stream().skip(1).forEach(row -> {
            LOG.trace(row.toString());

            // Verify that the row contains as many values as the header row.
            if (headerRow.size() < 2) {
                template.addError(String.format("Row [%s] must contain [%d] comma-separated values", row.toString(), row.size()));
            } else {

                String fieldString = row.get(fieldIndex);
                boolean fieldIsValid = StringUtils.isNotBlank(fieldString);
                if (!fieldIsValid) {
                    template.addError(String.format("The [%s] value must not be blank", FIELD));
                }

                String positionString = row.get(positionIndex);
                boolean positionIsValid = StringUtils.isNumeric(positionString);
                if (!positionIsValid) {
                    template.addError(String.format("[%s] is not a valid numeric value for [%s]", positionString, POSITION));
                }

                String typeString = row.get(typeIndex);
                boolean typeIsValid = EnumUtils.isValidEnum(FieldDescription.FIELD_TYPE.class, typeString);
                if (!typeIsValid) {
                    template.addError(String.format("[%s] is not a valid numeric value for [%s]. Valid values are [%s, %s, %s]", positionString, TYPE, BOOLEAN, LONG, STRING));
                }

                // All row values must be valid.
                if (fieldIsValid && positionIsValid && typeIsValid) {
                    template.getFields().add(new PANFieldTemplate(fieldString,
                                                                  Integer.valueOf(positionString),
                                                                  FieldDescription.FIELD_TYPE.valueOf(typeString)));
                }
            }
        });

        return template;
    }

    private static void checkErrors(PANMessageType messageType, List<String> errors) throws MisfireException {
        errors.add(0, String.format("Error validating the [%s] CSV message template:", messageType));
        throw new MisfireException(String.join("\n", errors));
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