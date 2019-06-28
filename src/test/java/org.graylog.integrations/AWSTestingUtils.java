package org.graylog.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.aws.codecs.KinesisCloudWatchFlowLogCodec;
import org.graylog.integrations.aws.codecs.KinesisRawLogCodec;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.graylog2.shared.bindings.providers.ObjectMapperProvider;

import java.util.HashMap;
import java.util.Map;

public class AWSTestingUtils {

    // Non-instantiable utils class.
    private AWSTestingUtils() {
    }

    public static Map<String, Codec.Factory<? extends Codec>> buildAWSCodecs() {

        // Prepare test codecs. These have to be manually instantiated for the test context.
        Map<String, Codec.Factory<? extends Codec>> availableCodecs = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapperProvider().get();
        availableCodecs.put(KinesisRawLogCodec.NAME, new KinesisRawLogCodec.Factory() {
            @Override
            public KinesisRawLogCodec create(Configuration configuration) {
                return new KinesisRawLogCodec(configuration, objectMapper);
            }

            @Override
            public KinesisRawLogCodec.Config getConfig() {
                return null;
            }

            @Override
            public Codec.Descriptor getDescriptor() {
                return null;
            }
        });

        availableCodecs.put(KinesisCloudWatchFlowLogCodec.NAME, new KinesisCloudWatchFlowLogCodec.Factory() {
            @Override
            public KinesisCloudWatchFlowLogCodec create(Configuration configuration) {
                return new KinesisCloudWatchFlowLogCodec(configuration, objectMapper);
            }

            @Override
            public KinesisCloudWatchFlowLogCodec.Config getConfig() {
                return null;
            }

            @Override
            public Codec.Descriptor getDescriptor() {
                return null;
            }
        });

        return availableCodecs;
    }
}
