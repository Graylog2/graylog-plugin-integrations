package org.graylog.integrations.aws.transports;

import com.codahale.metrics.MetricSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import org.graylog.integrations.aws.AWSUtils;
import org.graylog2.plugin.LocalMetricRegistry;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.DropdownField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.MisfireException;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.inputs.codecs.CodecAggregator;
import org.graylog2.plugin.inputs.transports.ThrottleableTransport;
import org.graylog2.plugin.inputs.transports.Transport;
import org.graylog2.plugin.journal.RawMessage;
import org.graylog2.plugin.system.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;

import javax.inject.Inject;
import java.util.function.Consumer;

public class KinesisTransport extends ThrottleableTransport {
    private static final Logger LOG = LoggerFactory.getLogger(KinesisTransport.class);
    public static final String NAME = "kinesis-transport";

    public static final String CK_KINESIS_STREAM_NAME = "kinesis_stream_name";
    public static final String CK_KINESIS_RECORD_BATCH_SIZE = "kinesis_record_batch_size";
    public static final String CK_KINESIS_MAX_THROTTLED_WAIT_MS = "kinesis_max_throttled_wait";

    private static final int DEFAULT_BATCH_SIZE = 10000;
    private static final int DEFAULT_THROTTLED_WAIT_MS = 60000;

    private final LocalMetricRegistry localRegistry;

    @Inject
    public KinesisTransport(@Assisted final Configuration configuration,
                            EventBus serverEventBus,
                            org.graylog2.Configuration graylogConfiguration,
                            final ClusterConfigService clusterConfigService,
                            final NodeId nodeId,
                            LocalMetricRegistry localRegistry,
                            ObjectMapper objectMapper) {
        super(serverEventBus, configuration);
        this.localRegistry = localRegistry;
    }

    @Override
    public void doLaunch(MessageInput input) throws MisfireException {
    }

    private Consumer<byte[]> kinesisCallback(final MessageInput input) {
        return (data) -> input.processRawMessage(new RawMessage(data));
    }

    @Override
    public void doStop() {

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
    public interface Factory extends Transport.Factory<KinesisTransport> {
        @Override
        KinesisTransport create(Configuration configuration);

        @Override
        Config getConfig();
    }

    @ConfigClass
    public static class Config extends ThrottleableTransport.Config {

        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            final ConfigurationRequest r = super.getRequestedConfiguration();

            r.addField(new NumberField(
                    CK_KINESIS_MAX_THROTTLED_WAIT_MS,
                    "Throttled wait milliseconds",
                    DEFAULT_THROTTLED_WAIT_MS,
                    "The maximum time that the Kinesis input will pause for when in a throttled state. If this time is exceeded, then the Kinesis consumer will shut down until the throttled state is cleared. Recommended default: 60,000 ms",
                    ConfigurationField.Optional.OPTIONAL,
                    NumberField.Attribute.ONLY_POSITIVE));

            r.addField(new TextField(
                    CK_KINESIS_STREAM_NAME,
                    "Kinesis Stream name",
                    "",
                    "The name of the Kinesis stream that receives your messages. See README for instructions on how to connect messages to a Kinesis Stream.",
                    ConfigurationField.Optional.NOT_OPTIONAL
            ));

            r.addField(new NumberField(
                    CK_KINESIS_RECORD_BATCH_SIZE,
                    "Kinesis Record batch size.",
                    DEFAULT_BATCH_SIZE,
                    "The number of Kinesis records to fetch at a time. Each record may be up to 1MB in size. The AWS default is 10,000. Enter a smaller value to process smaller chunks at a time.",
                    ConfigurationField.Optional.OPTIONAL,
                    NumberField.Attribute.ONLY_POSITIVE));

            return r;
        }
    }
}