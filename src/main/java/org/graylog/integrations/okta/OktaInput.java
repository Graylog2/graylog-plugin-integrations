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

import com.codahale.metrics.MetricRegistry;
import com.google.inject.assistedinject.Assisted;
import org.graylog2.plugin.LocalMetricRegistry;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.buffers.InputBuffer;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.MisfireException;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;

import javax.inject.Inject;


public class OktaInput extends MessageInput {

    public static final String NAME = "Okta";
    public static final String TYPE = "org.graylog.integrations.okta.OktaInput";

    public static final String CK_GLOBAL = "global";
    public static final String CK_OKTA_DOMAIN = "okta_domain";
    public static final String CK_OKTA_API_KEY = "okta_api_key";


    @Inject
    public OktaInput(@Assisted Configuration configuration,
                     MetricRegistry metricRegistry,
                     OktaTransport.Factory transport,
                     LocalMetricRegistry localRegistry,
                     OktaCodec.Factory codec,
                     Config config,
                     Descriptor descriptor,
                     ServerStatus serverStatus) {
        super(metricRegistry,
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
        super.launch(buffer);
    }

    @Override
    public void stop() {
        super.stop();
    }

    @FactoryClass
    public interface Factory extends MessageInput.Factory<OktaInput> {
        @Override
        OktaInput create(Configuration configuration);

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

        @Inject
        public Config(OktaTransport.Factory transport, OktaCodec.Factory codec) {
            super(transport.getConfig(), codec.getConfig());
        }

        @Override
        public ConfigurationRequest combinedRequestedConfiguration() {
            ConfigurationRequest request = super.combinedRequestedConfiguration();

            request.addField(new TextField(
                    CK_OKTA_DOMAIN,
                    "Okta Domain",
                    "",
                    ""));

            request.addField(new TextField(
                    CK_OKTA_API_KEY,
                    "Okta API Key",
                    "",
                    "",
                    TextField.Attribute.IS_PASSWORD));
            return request;
        }
    }
}
