package org.graylog.integrations.inputs.paloalto;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.assistedinject.Assisted;
import org.graylog2.inputs.transports.TcpTransport;
import org.graylog2.plugin.LocalMetricRegistry;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;

import javax.inject.Inject;

public class PaloAltoTCPInput extends MessageInput {

    private static final String NAME = "Palo Alto Networks Input (TCP)";

    @Inject
    public PaloAltoTCPInput(@Assisted Configuration configuration,
                            MetricRegistry metricRegistry,
                            TcpTransport.Factory transport,
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

    @FactoryClass
    public interface Factory extends MessageInput.Factory<PaloAltoTCPInput> {
        @Override
        PaloAltoTCPInput create(Configuration configuration);

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
        public Config(TcpTransport.Factory transport, PaloAltoCodec.Factory codec) {
            super(transport.getConfig(), codec.getConfig());
        }
    }
}
