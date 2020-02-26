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

import com.codahale.metrics.MetricSet;
import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import org.graylog2.plugin.LocalMetricRegistry;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.inputs.codecs.CodecAggregator;
import org.graylog2.plugin.inputs.transports.ThrottleableTransport;
import org.graylog2.plugin.inputs.transports.Transport;

import javax.inject.Inject;

// TODO determine if ThrottleableTransport is the most suitable transport to extend/use
public class OktaTransport extends ThrottleableTransport {
    public static final String NAME = "okta-transport";

    private LocalMetricRegistry localRegistry;
    private final Configuration configuration;


    public static final int DEFAULT_BATCH_SIZE = 10000;

    @Inject
    public OktaTransport(@Assisted Configuration configuration,
                         EventBus serverEventBus) {
        super(serverEventBus, configuration);
        this.configuration = configuration;
    }

    @Override
    public void doLaunch(MessageInput input) {
        // TODO implement proper launch method
    }

    @Override
    protected void doStop() {

    }

    @Override
    public void setMessageAggregator(CodecAggregator aggregator) {
        // Not supported.
    }

    @Override
    public MetricSet getMetricSet() {
        return localRegistry;
    }

    @FactoryClass
    public interface Factory extends Transport.Factory<OktaTransport> {
        @Override
        OktaTransport create(Configuration configuration);

        @Override
        Config getConfig();
    }

    public static class Descriptor extends MessageInput.Descriptor {
        public Descriptor() {
            super(NAME, false, "");
        }
    }

    @ConfigClass
    public static class Config extends ThrottleableTransport.Config {

        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            return super.getRequestedConfiguration();
        }
    }
}