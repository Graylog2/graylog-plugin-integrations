/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.integrations.okta;

import com.google.inject.assistedinject.Assisted;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.DropdownField;
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
import java.util.stream.Collectors;

public class OktaCodec extends AbstractCodec {

    public static final String NAME = "OktaCodec";
    private static final Logger LOG = LoggerFactory.getLogger(OktaCodec.class);

    public static final String CK_OKTA_MESSAGE_TYPE = "okta_message_type";
    public static final String CK_SYSTEM_LOG_PREFIX = "okta_system_log_prefix";

    static final boolean SYS_LOG_PREFIX_DEFAULT = true;

    private final Map<String, Codec.Factory<? extends Codec>> availableCodecs;

    @Inject
    public OktaCodec(@Assisted Configuration configuration,
                     Map<String, Codec.Factory<? extends Codec>> availableCodecs) {
        super(configuration);
        this.availableCodecs = availableCodecs;
    }

    @Nullable
    @Override
    public Message decode(@Nonnull RawMessage rawMessage) {

        // Load the codec by message type.
        final OktaMessageType oktaMessageType = OktaMessageType.valueOf(configuration.getString(CK_OKTA_MESSAGE_TYPE));
        final Codec.Factory<? extends Codec> codecFactory = this.availableCodecs.get(oktaMessageType.getCodecName());
        if (codecFactory == null) {
            LOG.error("A codec with name [{}] could not be found.", oktaMessageType.getCodecName());
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

    @Override
    public String getName() {
        return NAME;
    }

    @FactoryClass
    public interface Factory extends Codec.Factory<OktaCodec> {
        @Override
        OktaCodec create(Configuration configuration);

        @Override
        Config getConfig();
    }

    @ConfigClass
    public static class Config extends AbstractCodec.Config {
        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            ConfigurationRequest request = new ConfigurationRequest();

            request.addField(new DropdownField(
                    CK_OKTA_MESSAGE_TYPE,
                    "Okta Message Type",
                    "",
                    OktaMessageType.getMessageTypes().stream()
                                   .collect(Collectors.toMap(OktaMessageType::toString, OktaMessageType::getLabel)),
                    "The type of Okta message that this input will receive.",
                    ConfigurationField.Optional.NOT_OPTIONAL));

            request.addField(new BooleanField(
                    CK_SYSTEM_LOG_PREFIX,
                    "Add Okta System Log field name prefix",
                    SYS_LOG_PREFIX_DEFAULT,
                    "Add field with the System Log prefix e. g. \"src_addr\" -> \"sys_log_src_addr\"."
            ));
            return request;
        }
    }
}