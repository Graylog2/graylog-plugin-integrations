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
package org.graylog.integrations.aws.inputs;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.assistedinject.Assisted;
import org.graylog.integrations.aws.codecs.CloudWatchFlowLogCodec;
import org.graylog.integrations.aws.transports.KinesisTransport;
import org.graylog.integrations.inputs.paloalto.PaloAltoCodec;
import org.graylog.integrations.inputs.paloalto.PaloAltoTemplateDefaults;
import org.graylog.integrations.inputs.paloalto.PaloAltoTemplates;
import org.graylog2.inputs.transports.SyslogTcpTransport;
import org.graylog2.plugin.LocalMetricRegistry;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.buffers.InputBuffer;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.MisfireException;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.graylog.integrations.inputs.paloalto.PaloAltoCodec.CK_SYSTEM_TEMPLATE;
import static org.graylog.integrations.inputs.paloalto.PaloAltoCodec.CK_THREAT_TEMPLATE;
import static org.graylog.integrations.inputs.paloalto.PaloAltoCodec.CK_TRAFFIC_TEMPLATE;

public class AWSInput extends MessageInput {

    public static final String NAME = "AWS Input";
    public static final String TYPE = "org.graylog.integrations.aws.input.AWSInput";

    private static final Logger LOG = LoggerFactory.getLogger(AWSInput.class);

    @Inject
    public AWSInput(@Assisted Configuration configuration,
                    MetricRegistry metricRegistry,
                    KinesisTransport.Factory transport,
                    LocalMetricRegistry localRegistry,
                    PaloAltoCodec.Factory codec,
                    Config config,
                    Descriptor descriptor,
                    ServerStatus serverStatus) {
        super(
                metricRegistry,
                configuration,
                transport.create(configuration),
                localRegistry,
                codec.create(configuration),
                config,
                descriptor,
                serverStatus);
    }

    @Override
    public void launch(InputBuffer buffer) throws MisfireException {

        // Parse the templates to log any errors immediately on input startup.
        PaloAltoTemplates templates = PaloAltoTemplates.newInstance(configuration.getString(CK_SYSTEM_TEMPLATE, PaloAltoTemplateDefaults.SYSTEM_TEMPLATE),
                                                                    configuration.getString(CK_THREAT_TEMPLATE, PaloAltoTemplateDefaults.THREAT_TEMPLATE),
                                                                    configuration.getString(CK_TRAFFIC_TEMPLATE, PaloAltoTemplateDefaults.TRAFFIC_TEMPLATE));

        if (templates.hasErrors()) {
            throw new MisfireException(templates.errorMessageSummary("\n"));
        }

        super.launch(buffer);
    }

    @FactoryClass
    public interface Factory extends MessageInput.Factory<AWSInput> {
        @Override
        AWSInput create(Configuration configuration);

        @Override
        Config getConfig();

        @Override
        Descriptor getDescriptor();
    }

    public static class Descriptor extends MessageInput.Descriptor {
        public Descriptor() {
            super(NAME, false, "");
        }
    }

    @ConfigClass
    public static class Config extends MessageInput.Config {

        // TODO: Create metacodec and transport that dynamically picks the correct one based on the type of input.
        @Inject
        public Config(KinesisTransport.Factory transport, CloudWatchFlowLogCodec.Factory codec) {
            super(transport.getConfig(), codec.getConfig());
        }
    }
}
