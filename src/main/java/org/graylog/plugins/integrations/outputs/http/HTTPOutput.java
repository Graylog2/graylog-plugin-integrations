package org.graylog.plugins.integrations.outputs.http;

import com.google.inject.assistedinject.Assisted;
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

public class HTTPOutput implements MessageOutput {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPOutput.class);

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
    private static final String OUTPUT_DESCRIPTION = "An output that batch sends messages HTTP(S). Designed to be used with the Batched HTTP input.";
    private static final String OUTPUT_NAME = "Batched HTTP";

    private BatchedHttpProducer producer;

    @Inject
    public HTTPOutput(@Assisted Configuration configuration) throws MessageOutputConfigurationException {

        LOG.debug("Beginning initialization");

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

        LOG.debug("Initialization complete");
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

    public interface Factory extends MessageOutput.Factory<HTTPOutput> {
        @Override
        HTTPOutput create(Stream stream, Configuration configuration);

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
                    "https://www.example.org/batched-http",
                    "URL of batched http input (Note that the Graylog Batched HTTP input listens on the /batched-http resource)",
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
            super(OUTPUT_NAME, false, "", OUTPUT_DESCRIPTION);
        }
    }
}
