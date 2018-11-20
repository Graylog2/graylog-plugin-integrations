package org.graylog.integrations.inputs.paloalto.types;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.graylog2.plugin.inputs.MisfireException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.graylog.integrations.inputs.paloalto.types.FieldDescription.FIELD_TYPE.*;
import static org.graylog.integrations.inputs.paloalto.types.PANTemplateDefaults.*;

/**
 * Builds PAN message templates.
 */
public class PANTemplates {

    public static final String INVALID_TEMPLATE_ERROR = "[%s] Palo Alto input template is invalid.";
    private PANMessageTemplate systemMessageTemplate;
    private PANMessageTemplate threatMessageTemplate;
    private PANMessageTemplate trafficMessageTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(PANTemplates.class);

    public static PANTemplates newInstance(String systemCsv, String threatCsv, String trafficCsv) {

        // Use default templates if no template supplied.
        PANTemplates builder = new PANTemplates();
        String systemTemplate = StringUtils.isNotBlank(systemCsv) ? systemCsv : SYSTEM_TEMPLATE;
        String threatTemplate = StringUtils.isNotBlank(threatCsv) ? threatCsv : THREAT_TEMPLATE;
        String trafficTemplate = StringUtils.isNotBlank(trafficCsv) ? trafficCsv : TRAFFIC_TEMPLATE;

        builder.systemMessageTemplate = readCSV(systemTemplate, PANMessageType.SYSTEM);
        builder.threatMessageTemplate = readCSV(threatTemplate, PANMessageType.THREAT);
        builder.trafficMessageTemplate = readCSV(trafficTemplate, PANMessageType.TRAFFIC);

        return builder;
    }

    private static PANMessageTemplate readCSV(String csvString, PANMessageType messageType) {

        PANMessageTemplate template = new PANMessageTemplate();
        Reader stringReader = new StringReader(csvString);
        CSVParser parser = null;
        List<CSVRecord> list = null;
        try {
            parser = new CSVParser(stringReader, CSVFormat.DEFAULT);
            list = parser.getRecords();
        } catch (IOException e) {
            template.addError(String.format("Failed to parse [%s] CSV. Error [%s/%s] CSV [%s].",
                                            messageType, ExceptionUtils.getMessage(e), ExceptionUtils.getRootCause(e), csvString));

            return template;
        }

        // Periodically check errors to provide as much feedback to the user as possible about any misconfiguration.
        if (list.isEmpty()) {
            template.addError(String.format("The header row is missing. It must include the following fields: [%s,%s,%s].", POSITION, FIELD, TYPE));
        }

        if (template.hasErrors()) {
            return template;
        }

        if (!list.stream().findFirst().filter(row -> row.toString().contains(POSITION)).isPresent()) {
            template.addError(String.format("The header row is invalid. It must include the [%s] field.", POSITION));
        }

        if (!list.stream().findFirst().filter(row -> row.toString().contains(FIELD)).isPresent()) {
            template.addError(String.format("The header row is invalid. It must include the [%s] field.", FIELD));
        }

        if (!list.stream().findFirst().filter(row -> row.toString().contains(TYPE)).isPresent()) {
            template.addError(String.format("The header row is invalid. It must include the [%s] field.", TYPE));
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
            LOG.error(String.format("No fields were specified for the [%s] message type.", messageType));
            return template;
        }

        // Skip header row.
        LOG.trace("Parsing CSV rows");
        int rowIndex = 0;
        for (CSVRecord row : list) {
            rowIndex++;
            if (rowIndex == 1) {
                continue;
            }

            LOG.trace(row.toString());

            // Verify that the row contains as many values as the header row.
            if (headerRow.size() < 2) {
                template.addError(String.format("LINE %d: Row [%s] must contain [%d] comma-separated values", rowIndex, row.toString(), row.size()));
            } else {

                String fieldString = row.size() >= 1 ? row.get(fieldIndex) : "";
                boolean fieldIsValid = StringUtils.isNotBlank(fieldString);
                if (!fieldIsValid) {
                    template.addError(String.format("LINE %d: The [%s] value must not be blank", rowIndex, FIELD));
                }

                String positionString = row.size() >= 2 ? row.get(positionIndex) : "";
                boolean positionIsValid = StringUtils.isNumeric(positionString);
                if (!positionIsValid) {
                    template.addError(String.format("LINE %d: [%s] is not a valid positive integer value for [%s]", rowIndex, positionString, POSITION));
                }

                String typeString = row.size() >= 3 ? row.get(typeIndex) : "";
                boolean typeIsValid = EnumUtils.isValidEnum(FieldDescription.FIELD_TYPE.class, typeString);
                if (!typeIsValid) {
                    template.addError(String.format("LINE %d: [%s] is not a valid [%s] value. Valid values are [%s, %s, %s]", rowIndex, typeString, TYPE, BOOLEAN, LONG, STRING));
                }

                // All row values must be valid.
                if (fieldIsValid && positionIsValid && typeIsValid) {
                    template.getFields().add(new PANFieldTemplate(fieldString,
                                                                  Integer.valueOf(positionString),
                                                                  FieldDescription.FIELD_TYPE.valueOf(typeString)));
                }
            }
        }

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

    public List<String> getAllErrors() {

        ArrayList<String> errors = new ArrayList<>();
        if (systemMessageTemplate != null) {
            errors.addAll(systemMessageTemplate.getParseErrors());
        }
        if (threatMessageTemplate != null) {
            errors.addAll(threatMessageTemplate.getParseErrors());
        }

        if (trafficMessageTemplate.getParseErrors() != null) {
            errors.addAll(trafficMessageTemplate.getParseErrors());
        }
        return errors;
    }

    public String errorMessageSummary(String delimiter) {

        ArrayList<String> errors = new ArrayList<>();
        if (systemMessageTemplate != null) {
            errors.add(String.format(INVALID_TEMPLATE_ERROR, PANMessageType.SYSTEM));
            errors.addAll(systemMessageTemplate.getParseErrors());
        }
        if (threatMessageTemplate != null) {
            errors.add(String.format(INVALID_TEMPLATE_ERROR, PANMessageType.THREAT));
            errors.addAll(threatMessageTemplate.getParseErrors());
        }

        if (trafficMessageTemplate.getParseErrors() != null) {
            errors.add(String.format(INVALID_TEMPLATE_ERROR, PANMessageType.THREAT));
            errors.addAll(trafficMessageTemplate.getParseErrors());
        }
        return errors.stream().collect(Collectors.joining(delimiter));
    }

    public boolean hasErrors() {

        return !getAllErrors().isEmpty();
    }
}