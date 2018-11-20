package org.graylog.integrations.inputs.paloalto.types;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.graylog2.plugin.inputs.MisfireException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.IntStream;

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
        CSVRecord row = list.get(0);

        // All indexes will be non-null, since we've already verify that they exist.
        int positionIndex = IntStream.range(0, row.size())
                                     .filter(i -> POSITION.equals(row.get(i)))
                                     .findFirst().getAsInt();

        int fieldIndex = IntStream.range(0, row.size())
                                  .filter(i -> FIELD.equals(row.get(i)))
                                  .findFirst().getAsInt();

        int typeIndex = IntStream.range(0, row.size())
                                 .filter(i -> TYPE.equals(row.get(i)))
                                 .findFirst().getAsInt();

        // Skip header row.
        LOG.trace("Parsing CSV rows");
        list.stream().skip(1).forEach(aRow -> {
            LOG.trace(aRow.toString());
            template.getFields().add(new PANFieldTemplate(aRow.get(fieldIndex),
                                                          Integer.valueOf(aRow.get(positionIndex)),
                                                          FieldDescription.FIELD_TYPE.valueOf(aRow.get(typeIndex))));
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