package org.graylog.plugins.integrations.outputs.http;

import com.google.inject.assistedinject.Assisted;
import org.graylog2.outputs.GelfOutput;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.outputs.MessageOutputConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

public class GELFHTTPOutput implements MessageOutput {

    private static final Logger LOG = LoggerFactory.getLogger(GelfOutput.class);

    private static final int BATCH_SIZE_DEFAULT = 250;
    private static final int BATCH_TIMEOUT_DEFAULT = 5000;
    private static final int CONNECT_TIMEOUT_DEFAULT = 2000;
    private static final int READ_TIMEOUT_DEFAULT = 1000;
    private static final int THREAD_POOL_SIZE_DEFAULT = 5;
    private static final int WRITE_TIMEOUT_DEFAULT = 2000;

    private static final String CK_BATCH_SIZE = "batch_size";
    private static final String CK_BATCH_TIMEOUT = "batch_timeout";
    private static final String CK_CONNECT_TIMEOUT = "connect_timeout";
    private static final String CK_ENABLE_GZIP = "enable_gzip";
    private static final String CK_READ_TIMEOUT = "read_timeout";
    private static final String CK_THREAD_POOL_SIZE = "thread_pool_size";
    private static final String CK_URL = "url";
    private static final String CK_WRITE_TIMEOUT = "write_timeout";
    private static final String GELF_HTTP_OUTPUT_DESCRIPTION = "An output sending GELF over HTTP(S)";
    private static final String GELF_HTTP_OUTPUT_NAME = "GELF Output (HTTP)";

    private BatchedHttpProducer producer;

    @Inject
    public GELFHTTPOutput(@Assisted Configuration configuration) throws MessageOutputConfigurationException {

        LOG.debug("Output initialized.");

        String rawUrl = configuration.getString(CK_URL);
        if (rawUrl == null) {
            throw new MessageOutputConfigurationException("Required parameter [" + CK_URL + "] not set.");
        }

        producer = new BatchedHttpProducer(
                configuration.getInt(CK_BATCH_SIZE, BATCH_SIZE_DEFAULT),
                rawUrl,
                configuration.getInt(CK_BATCH_TIMEOUT, BATCH_TIMEOUT_DEFAULT),
                configuration.getInt(CK_THREAD_POOL_SIZE),
                configuration.getBoolean(CK_ENABLE_GZIP), configuration.getInt(CK_WRITE_TIMEOUT, WRITE_TIMEOUT_DEFAULT), configuration.getInt(CK_READ_TIMEOUT, READ_TIMEOUT_DEFAULT),
                configuration.getInt(CK_CONNECT_TIMEOUT, CONNECT_TIMEOUT_DEFAULT));
        producer.start();

        LOG.info("Start done.");
    }

    @Override
    public boolean isRunning() {
        return producer != null && producer.isRunning();
    }

    @Override
    public void write(Message message) throws Exception {

        LOG.trace("Writing message to producer [{}]", message);
        producer.writeMessage(message);
    }

    @Override
    public void write(List<Message> list) throws Exception {
        for (Message message : list) {
            write(message);
        }
    }

    @Override
    public void stop() {
        producer.stop();
    }


    public interface Factory extends MessageOutput.Factory<GELFHTTPOutput> {
        @Override
        GELFHTTPOutput create(Stream stream, Configuration configuration);

        @Override
        Config getConfig();

        @Override
        Descriptor getDescriptor();
    }

    public static class Config extends MessageOutput.Config {
        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            final ConfigurationRequest configurationRequest = new ConfigurationRequest();

            configurationRequest.addField(new TextField(
                    CK_URL,
                    "URL",
                    "https://www.example.org/gelf",
                    "URL of GELF input (Note that the Graylog GELF HTTP input listens on the /gelf resource)",
                    ConfigurationField.Optional.NOT_OPTIONAL
            ));

            configurationRequest.addField(new NumberField(
                    CK_CONNECT_TIMEOUT,
                    "Connect Timeout",
                    CONNECT_TIMEOUT_DEFAULT,
                    "Connect timeout in milliseconds",
                    ConfigurationField.Optional.NOT_OPTIONAL,
                    NumberField.Attribute.ONLY_POSITIVE)
            );

            configurationRequest.addField(new NumberField(
                    CK_READ_TIMEOUT,
                    "Read Timeout",
                    READ_TIMEOUT_DEFAULT,
                    "Read timeout in milliseconds",
                    ConfigurationField.Optional.NOT_OPTIONAL,
                    NumberField.Attribute.ONLY_POSITIVE)
            );

            configurationRequest.addField(new NumberField(
                    CK_WRITE_TIMEOUT,
                    "Write Timeout",
                    WRITE_TIMEOUT_DEFAULT,
                    "Write timeout in milliseconds",
                    ConfigurationField.Optional.NOT_OPTIONAL,
                    NumberField.Attribute.ONLY_POSITIVE)
            );

            configurationRequest.addField(new NumberField(
                    CK_THREAD_POOL_SIZE,
                    "Thread Pool Size",
                    THREAD_POOL_SIZE_DEFAULT,
                    "How large should the writer thread pool be? Increase this number if you are seeing throughput issues. (Note that each outputbuffer_processor has it's own thread pool)",
                    ConfigurationField.Optional.NOT_OPTIONAL,
                    NumberField.Attribute.ONLY_POSITIVE)
            );

            configurationRequest.addField(new BooleanField(
                    CK_ENABLE_GZIP,
                    "Enable GZIP",
                    false,
                    "Enable GZIP compression?"
            ));

            configurationRequest.addField(new NumberField(
                    CK_BATCH_SIZE,
                    "Batch Size",
                    BATCH_SIZE_DEFAULT,
                    "How many messages to send per HTTP request. The output will either wait for the batch size to be reached or for the batch timeout to be hit before sending a batch of messages.",
                    ConfigurationField.Optional.NOT_OPTIONAL,
                    NumberField.Attribute.ONLY_POSITIVE)
            );

            configurationRequest.addField(new NumberField(
                    CK_BATCH_TIMEOUT,
                    "Batch Timeout (ms)",
                    BATCH_TIMEOUT_DEFAULT,
                    "How many milliseconds to wait until we are sending a not yet filled batch. See \"Batch Size\" option.",
                    ConfigurationField.Optional.NOT_OPTIONAL,
                    NumberField.Attribute.ONLY_POSITIVE)
            );

            return configurationRequest;
        }
    }

    public static class Descriptor extends MessageOutput.Descriptor {
        public Descriptor() {
            super(GELF_HTTP_OUTPUT_NAME, false, "", GELF_HTTP_OUTPUT_DESCRIPTION);
        }
    }
}
