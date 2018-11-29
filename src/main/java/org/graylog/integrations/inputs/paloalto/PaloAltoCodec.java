package org.graylog.integrations.inputs.paloalto;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.graylog2.plugin.inputs.codecs.CodecAggregator;
import org.graylog2.plugin.journal.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.graylog.integrations.inputs.paloalto.PaloAltoMessageType.SYSTEM;
import static org.graylog.integrations.inputs.paloalto.PaloAltoMessageType.THREAT;
import static org.graylog.integrations.inputs.paloalto.PaloAltoMessageType.TRAFFIC;

public class PaloAltoCodec implements Codec {

    public static final String NAME = "PaloAlto";

    public static final String CK_TRAFFIC_TEMPLATE = "TRAFFIC_TEMPLATE";
    public static final String CK_THREAT_TEMPLATE = "THREAT_TEMPLATE";
    public static final String CK_SYSTEM_TEMPLATE = "SYSTEM_TEMPLATE";

    private static final Logger LOG = LoggerFactory.getLogger(PaloAltoCodec.class);

    private final Configuration configuration;
    private final PaloAltoParser parser;
    private final PaloAltoTemplates templates;

    @AssistedInject
    public PaloAltoCodec(@Assisted Configuration configuration) {
        this.configuration = configuration;
        this.parser = new PaloAltoParser();
        this.templates = PaloAltoTemplates.newInstance(configuration.getString(CK_SYSTEM_TEMPLATE, PaloAltoTemplateDefaults.SYSTEM_TEMPLATE),
                                                       configuration.getString(CK_THREAT_TEMPLATE, PaloAltoTemplateDefaults.THREAT_TEMPLATE),
                                                       configuration.getString(CK_TRAFFIC_TEMPLATE, PaloAltoTemplateDefaults.TRAFFIC_TEMPLATE));
    }

    @Nullable
    @Override
    public Message decode(@Nonnull RawMessage rawMessage) {
        String s = new String(rawMessage.getPayload());
        LOG.debug("Received raw message: {}", s);

        PaloAltoMessageBase p = parser.parse(s);

        if (p == null) {
            LOG.warn("Could not parse PAN message.");
            return null;
        }

        Message message = new Message(p.payload(), p.source(), p.timestamp());

        switch (p.panType()) {
            case "THREAT":
                final PaloAltoTypeParser parserThreat = new PaloAltoTypeParser(templates.getThreatMessageTemplate(), THREAT);
                message.addFields(parserThreat.parseFields(p.fields()));
                break;
            case "SYSTEM":
                final PaloAltoTypeParser parserSystem = new PaloAltoTypeParser(templates.getSystemMessageTemplate(), SYSTEM);
                message.addFields(parserSystem.parseFields(p.fields()));
                break;
            case "TRAFFIC":
                final PaloAltoTypeParser parserTraffic = new PaloAltoTypeParser(templates.getTrafficMessageTemplate(), TRAFFIC);
                message.addFields(parserTraffic.parseFields(p.fields()));
                break;
            default:
                LOG.debug("Unsupported PAN type [{}]. Not adding any parsed fields.", p.panType());
        }

        return message;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Nonnull
    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @FactoryClass
    public interface Factory extends Codec.Factory<PaloAltoCodec> {
        @Override
        PaloAltoCodec create(Configuration configuration);

        @Override
        Config getConfig();
    }

    @ConfigClass
    public static class Config implements Codec.Config {

        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            final ConfigurationRequest request = new ConfigurationRequest();

            request.addField(new TextField(
                    CK_SYSTEM_TEMPLATE,
                    "System Message Template",
                    PaloAltoTemplateDefaults.SYSTEM_TEMPLATE,
                    "CSV string representing the fields/positions/data types to parse. (See documentation)",
                    ConfigurationField.Optional.OPTIONAL, TextField.Attribute.TEXTAREA));

            request.addField(new TextField(
                    CK_THREAT_TEMPLATE,
                    "Threat Message Template",
                    PaloAltoTemplateDefaults.THREAT_TEMPLATE,
                    "CSV string representing the fields/positions/data types to parse. (See documentation)",
                    ConfigurationField.Optional.OPTIONAL, TextField.Attribute.TEXTAREA));

            request.addField(new TextField(
                    CK_TRAFFIC_TEMPLATE,
                    "Traffic Message Template",
                    PaloAltoTemplateDefaults.TRAFFIC_TEMPLATE,
                    "CSV representing the fields/positions/data types to parse. (See documentation)",
                    ConfigurationField.Optional.OPTIONAL, TextField.Attribute.TEXTAREA));

            return request;
        }

        @Override
        public void overrideDefaultValues(@Nonnull ConfigurationRequest cr) {
        }


    }

    @Nullable
    @Override
    public CodecAggregator getAggregator() {
        return null;
    }

}
