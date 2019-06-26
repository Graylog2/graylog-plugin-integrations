package org.graylog.integrations.aws.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.assistedinject.Assisted;
import org.graylog.integrations.aws.cloudwatch.KinesisLogEntry;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.inputs.codecs.AbstractCodec;
import org.graylog2.plugin.inputs.codecs.Codec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class AWSMetaCodec extends KinesisLogDataCodec {
    public static final String NAME = "AWSMetaCodec";

    @Inject
    public AWSMetaCodec(@Assisted Configuration configuration, ObjectMapper objectMapper) {
        super(configuration, objectMapper);
    }

    @Nullable
    @Override
    public Message decodeLogData(@Nonnull final KinesisLogEntry logEvent) {

        // TODO: Select the correct codec and and decode the message.
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @FactoryClass
    public interface Factory extends Codec.Factory<AWSMetaCodec> {
        @Override
        AWSMetaCodec create(Configuration configuration);

        @Override
        Config getConfig();
    }

    @ConfigClass
    public static class Config extends AbstractCodec.Config {
        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            return new ConfigurationRequest();
        }

        @Override
        public void overrideDefaultValues(@Nonnull ConfigurationRequest cr) {
        }
    }
}
