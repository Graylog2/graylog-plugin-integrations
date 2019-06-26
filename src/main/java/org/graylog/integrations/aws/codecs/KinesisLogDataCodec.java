package org.graylog.integrations.aws.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.aws.cloudwatch.KinesisLogEntry;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.codecs.AbstractCodec;
import org.graylog2.plugin.inputs.codecs.CodecAggregator;
import org.graylog2.plugin.journal.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public abstract class KinesisLogDataCodec extends AbstractCodec {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisLogDataCodec.class);

    public static final String SOURCE_GROUP_IDENTIFIER = "aws_source";
    public static final String FIELD_KINESIS_STREAM = "aws_kinesis_stream";
    public static final String FIELD_LOG_GROUP = "aws_log_group";
    public static final String FIELD_LOG_STREAM = "aws_log_stream";

    private final ObjectMapper objectMapper;

    KinesisLogDataCodec(Configuration configuration, ObjectMapper objectMapper) {
        super(configuration);
        this.objectMapper = objectMapper;
    }

    @Nullable
    @Override
    public Message decode(@Nonnull RawMessage rawMessage) {
        try {
            final KinesisLogEntry entry = objectMapper.readValue(rawMessage.getPayload(), KinesisLogEntry.class);

            try {
                return decodeLogData(entry);
            } catch (Exception e) {
                LOG.error("Couldn't decode log event <{}>", entry);

                // Message will be dropped when returning null
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't deserialize log data", e);
        }
    }

    @Nullable
    protected abstract Message decodeLogData(@Nonnull final KinesisLogEntry event);

    @Nonnull
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Nullable
    @Override
    public CodecAggregator getAggregator() {
        return null;
    }
}
