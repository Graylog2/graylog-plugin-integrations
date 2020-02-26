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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

// TODO determine if ThrottleableTransport is the most suitable transport to extend/use
public class OktaTransport extends ThrottleableTransport {
    private static final Logger LOG = LoggerFactory.getLogger(OktaTransport.class);
    public static final String NAME = "okta-transport";

    private LocalMetricRegistry localRegistry;
    private final Configuration configuration;

    private static final String CK_OKTA_DOMAIN = "okta_domain";
    private static final String CK_OKTA_API_KEY = "okta_api_key";


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