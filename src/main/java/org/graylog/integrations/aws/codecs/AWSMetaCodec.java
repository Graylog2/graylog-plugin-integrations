package org.graylog.integrations.aws.codecs;

import com.google.inject.assistedinject.Assisted;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.inputs.AWSInput;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.inputs.codecs.AbstractCodec;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.graylog2.plugin.journal.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Map;

public class AWSMetaCodec extends AbstractCodec {

    public static final String NAME = "AWSMetaCodec";
    private static final Logger LOG = LoggerFactory.getLogger(AWSMetaCodec.class);

    private final Map<String, Codec.Factory<? extends Codec>> availableCodecs;

    @Inject
    public AWSMetaCodec(@Assisted Configuration configuration,
                        Map<String, Codec.Factory<? extends Codec>> availableCodecs) {
        super(configuration);
        this.availableCodecs = availableCodecs;
    }

    @Nullable
    @Override
    public Message decode(@Nonnull RawMessage rawMessage) {

        // Load the codec by message type.
        final AWSMessageType awsMessageType = AWSMessageType.valueOf(configuration.getString(AWSInput.CK_AWS_MESSAGE_TYPE));
        final Codec.Factory<? extends Codec> codecFactory = this.availableCodecs.get(awsMessageType.getCodecName());
        if (codecFactory == null) {
            LOG.error("A codec with name [{}] could not be found.", awsMessageType.getCodecName());
            return null;
        }

        final Codec codec = codecFactory.create(configuration);

        // Parse the message with the specified codec.
        final Message message = codec.decode(new RawMessage(rawMessage.getPayload()));
        if (message == null) {
            LOG.error("Failed to decode message for codec [{}].", codec.getName());
            return null;
        }

        return message;
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