package org.graylog.integrations.inputs.paloalto;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.graylog.integrations.inputs.paloalto.types.PANTemplateBuilder;
import org.graylog.integrations.inputs.paloalto.types.PANTemplateDefaults;
import org.graylog.integrations.inputs.paloalto.types.SystemMessageMapping;
import org.graylog.integrations.inputs.paloalto.types.ThreatMessageMapping;
import org.graylog.integrations.inputs.paloalto.types.TrafficMessageMapping;
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
import java.io.IOException;

import static org.graylog.integrations.inputs.paloalto.types.PANMessageType.*;

public class PaloAltoCodec implements Codec {

    public static final String NAME = "PaloAlto";

    private static final Logger LOG = LoggerFactory.getLogger(PaloAltoCodec.class);

    private final Configuration configuration;
    private final PANParser parser;


    @AssistedInject
    public PaloAltoCodec(@Assisted Configuration configuration) {
        this.configuration = configuration;
        this.parser = new PANParser();
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

        final PANTemplateBuilder builder;
        try {
            builder = PANTemplateBuilder.newInstance(null, null, null);
        } catch (IOException e) {
            // TODO: Handle exception.
            return null;
        }

        switch (p.panType()) {
            case "THREAT":
                final PANTypeParser PARSER_THREAT = new PANTypeParser(new ThreatMessageMapping(), builder.getThreatMessageTemplate(), THREAT);
                message.addFields(PARSER_THREAT.parseFields(p.fields()));
                break;
            case "SYSTEM":
                final PANTypeParser PARSER_SYSTEM = new PANTypeParser(new SystemMessageMapping(), builder.getSystemMessageTemplate(), SYSTEM);
                message.addFields(PARSER_SYSTEM.parseFields(p.fields()));
                break;
            case "TRAFFIC":
                final PANTypeParser PARSER_TRAFFIC = new PANTypeParser(new TrafficMessageMapping(), builder.getTrafficMessageTemplate(), TRAFFIC);
                message.addFields(PARSER_TRAFFIC.parseFields(p.fields()));
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
                    "SYSTEM_TEMPLATE",
                    "System Message Template",
                    PANTemplateDefaults.SYSTEM_TEMPLATE,
                    "CSV string representing the fields/positions/data types to parse. (See documentation)",
                    ConfigurationField.Optional.OPTIONAL, TextField.Attribute.TEXTAREA ));

            request.addField(new TextField(
                    "THREAT_TEMPLATE",
                    "Threat Message Template",
                    PANTemplateDefaults.THREAT_TEMPLATE,
                    "CSV string representing the fields/positions/data types to parse. (See documentation)",
                    ConfigurationField.Optional.OPTIONAL, TextField.Attribute.TEXTAREA ));

            request.addField(new TextField(
                    "TRAFFIC_TEMPLATE",
                    "Traffic Message Template",
                    PANTemplateDefaults.TRAFFIC_TEMPLATE,
                    "CSV representing the fields/positions/data types to parse. (See documentation)",
                    ConfigurationField.Optional.OPTIONAL, TextField.Attribute.TEXTAREA ));

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
