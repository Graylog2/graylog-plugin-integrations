package org.graylog.integrations.aws.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.assistedinject.Assisted;
import org.graylog.integrations.aws.AWSUtils;
import org.graylog.integrations.aws.cloudwatch.CloudWatchLogEntry;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.inputs.codecs.AbstractCodec;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class CloudWatchRawLogCodec extends CloudWatchLogDataCodec {
    public static final String NAME = "CloudWatchRawLog";

    @Inject
    public CloudWatchRawLogCodec(@Assisted Configuration configuration, ObjectMapper objectMapper) {
        super(configuration, objectMapper);
    }

    @Nullable
    @Override
    public Message decodeLogData(@Nonnull final CloudWatchLogEntry logEvent) {
        try {
            final String source = configuration.getString(CloudWatchFlowLogCodec.Config.CK_OVERRIDE_SOURCE, "aws-raw-logs");
            Message result = new Message(
                    logEvent.message(),
                    source,
                    new DateTime(logEvent.timestamp())
            );

            logEvent.logGroup().ifPresent(group -> result.addField(AWSUtils.FIELD_LOG_GROUP, group));
            logEvent.logStream().ifPresent(stream -> result.addField(AWSUtils.FIELD_LOG_STREAM, stream));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Could not deserialize AWS FlowLog record.", e);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @FactoryClass
    public interface Factory extends Codec.Factory<CloudWatchRawLogCodec> {
        @Override
        CloudWatchRawLogCodec create(Configuration configuration);

        @Override
        Config getConfig();
    }

    @ConfigClass
    public static class Config extends AbstractCodec.Config {
    }
}
