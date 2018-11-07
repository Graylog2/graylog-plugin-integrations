package org.graylog.plugins.integrations.outputs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.assistedinject.Assisted;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfMessageLevel;
import org.graylog2.outputs.GelfOutput;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.outputs.MessageOutputConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GELFHTTPOutput implements MessageOutput {

    private static final Logger LOG = LoggerFactory.getLogger(GelfOutput.class);

    private static final int RETRY_TIME_MS = 2000;

    private static final int CONNECT_TIMEOUT_DEFAULT = 2000;
    private static final int READ_TIMEOUT_DEFAULT = 1000;
    private static final int WRITE_TIMEOUT_DEFAULT = 2000;
    private static final int THREAD_POOL_SIZE_DEFAULT = 5;
    private static final int BATCH_SIZE_DEFAULT = 250;
    private static final int BATCH_TIMEOUT_DEFAULT = 5000;

    private static final String CK_CONNECT_TIMEOUT = "connect_timeout";
    private static final String CK_READ_TIMEOUT = "read_timeout";
    private static final String CK_WRITE_TIMEOUT = "write_timeout";
    private static final String CK_URL = "url";
    private static final String CK_THREAD_POOL_SIZE = "thread_pool_size";
    private static final String CK_ENABLE_GZIP = "enable_gzip";
    private static final String CK_BATCH_SIZE = "batch_size";
    private static final String CK_BATCH_TIMEOUT = "batch_timeout";
    private static final String CONTENT_ENCODING = "Content-Encoding";

    private static final String GZIP_ENCODING = "gzip";
    private static final String GELF_HTTP_OUTPUT_NAME = "GELF Output (HTTP)";
    private static final String GELF_HTTP_OUTPUT_DESCRIPTION = "An output sending GELF over HTTP(S)";
    private static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String GRAYLOG_OUTPUT_USER_AGENT = "graylog-output-gelf-http";
    private static final int MILLIS = 5;

    private final AtomicBoolean isRunning;

    private final OkHttpClient httpClient;
    private final HttpUrl url;

    private final ObjectMapper om;

    private final ExecutorService executor;

    private final int maximumThreads;
    private final AtomicInteger activeThreads;

    private final int maximumBatchSize;
    private final int batchTimeout;

    private final Queue<Map<String, Object>> batch;

    private DateTime lastBatchWrite;

    @Inject
    public GELFHTTPOutput(@Assisted Configuration configuration) throws MessageOutputConfigurationException {
        this.isRunning = new AtomicBoolean(true);
        this.om = new ObjectMapper(); // Not using injected OM because we need specific (default) settings.
        this.activeThreads = new AtomicInteger(0);

        this.batch = new ConcurrentLinkedQueue<>();
        this.maximumBatchSize = configuration.getInt(CK_BATCH_SIZE, BATCH_SIZE_DEFAULT);
        this.batchTimeout = configuration.getInt(CK_BATCH_TIMEOUT, BATCH_TIMEOUT_DEFAULT);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(configuration.getInt(CK_CONNECT_TIMEOUT, CONNECT_TIMEOUT_DEFAULT), TimeUnit.MILLISECONDS)
                .connectTimeout(configuration.getInt(CK_READ_TIMEOUT, READ_TIMEOUT_DEFAULT), TimeUnit.MILLISECONDS)
                .connectTimeout(configuration.getInt(CK_WRITE_TIMEOUT, WRITE_TIMEOUT_DEFAULT), TimeUnit.MILLISECONDS);

        if (configuration.getBoolean(CK_ENABLE_GZIP)) {
            builder.addInterceptor(new GzipRequestInterceptor());
        }

        this.httpClient = builder.build();

        String rawUrl = configuration.getString(CK_URL);
        if (rawUrl == null) {
            throw new MessageOutputConfigurationException("Required parameter [" + CK_URL + "] not set.");
        }

        this.url = HttpUrl.parse(rawUrl);

        this.maximumThreads = configuration.getInt(CK_THREAD_POOL_SIZE);
        this.executor = Executors.newFixedThreadPool(
                this.maximumThreads,
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("gelf-http-output-sender-%d")
                        .build() );

        // Schedule writing of batches.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                                                           .setDaemon(true)
                                                           .setNameFormat("gelf-http-output-batch-scheduler-%d")
                                                           .build())
                 .scheduleWithFixedDelay(writeBatchIfReady(), 0, 50, TimeUnit.MILLISECONDS);
    }

    private Runnable writeBatchIfReady() {
        return () -> {
            try {
                // Wait until we have enough messages or the wait timeout has passed.
                long size = batch.size();

                if (size >= maximumBatchSize || inBatchTimeout()) {
                    final List<Map<String, Object>> slice = Lists.newArrayList();
                    for (int i = 0; i < maximumBatchSize; i++) {
                        Map<String, Object> message = batch.poll();
                        if (message == null) {
                            break;
                        } else {
                            slice.add(message);
                        }
                    }

                    if (slice.isEmpty()) {
                        return;
                    }

                    LOG.debug("Sending slice of <{}> messages from batch of <{}>.", slice.size(), size);
                    executor.submit(() -> {
                        lastBatchWrite = DateTime.now();
                        activeThreads.incrementAndGet();

                        BatchedRequest batchedRequest = new BatchedRequest(slice);

                        // We will retry messages that were not accepted. The OutputBuffer timeout has to be adapted properly, based on the use-case.
                        while (true) {
                            byte[] body;
                            try {
                                body = om.writeValueAsBytes(batchedRequest);
                            } catch (JsonProcessingException e) {
                                LOG.error("Cannot build JSON out of message batch. This is irrecoverable. Skipping batch.", e);
                                break;
                            }

                            Request request = new Request.Builder()
                                    .post(RequestBody.create(MediaType.parse(APPLICATION_JSON_CONTENT_TYPE), body))
                                    .url(url)
                                    .addHeader(USER_AGENT_HEADER, GRAYLOG_OUTPUT_USER_AGENT)
                                    .build();

                            Response response = null;
                            try {
                                response = httpClient.newCall(request).execute();

                                if (response.isSuccessful()) {
                                    LOG.debug("GELF messages written successfully with HTTP response code [{}].", response.code());
                                    break;
                                } else {
                                    LOG.warn("Could not write GELF messages to [{}]. Received HTTP response code [{}]. Will retry.", url.toString(), response.code());
                                    try {
                                        Thread.sleep(RETRY_TIME_MS);
                                    } catch (InterruptedException e1) { /* noop */ }
                                }
                            } catch (Exception e) {
                                LOG.warn("Could not write GELF messages to [{}]. Will retry. Reason was: {}", url.toString(), e.getCause());
                                LOG.debug("Full exception trace:", e);
                                try {
                                    Thread.sleep(RETRY_TIME_MS);
                                } catch (InterruptedException e1) { /* noop */ }
                            } finally {
                                if (response != null) {
                                    response.close();
                                }
                            }
                        }

                        // The whole slice of the batch has been successfully transmitted.
                        activeThreads.decrementAndGet();
                    });
                } else {
                    LOG.debug("Not writing batch: Timeout or batch size not reached yet.");
                }
            } catch (Exception e) {
                LOG.error("Failure when attempting to write batch.", e);
            }
        };
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public void write(Message message) throws Exception {
        // Block until a sender thread is available. (this is definitely not handling possible race-conditions,
        // but possibly submitting one task too much is a calculated risk here) - see comment on top of this class.
        while (true) {
            if (this.activeThreads.get() >= this.maximumThreads) {
                try {
                    Thread.sleep(MILLIS);
                } catch (InterruptedException e) { /* noop */ }
            } else {
                break;
            }
        }

        this.batch.add(toGELFMessage(message));
    }

    private boolean inBatchTimeout() {
        if (lastBatchWrite == null) {
            LOG.debug("First batch time recording.");
            return true;
        }

        if (lastBatchWrite.isBefore(DateTime.now().minusMillis(batchTimeout))) {
            LOG.debug("Batch timeout reached!");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void write(List<Message> list) throws Exception {
        for (Message message : list) {
            write(message);
        }
    }

    @Override
    public void stop() {
        this.isRunning.set(false);
    }

    @Nullable
    private GelfMessageLevel extractLevel(Object rawLevel) {
        GelfMessageLevel level;
        if (rawLevel instanceof Number) {
            final int numericLevel = ((Number) rawLevel).intValue();
            level = extractLevel(numericLevel);
        } else if (rawLevel instanceof String) {
            Integer numericLevel;
            try {
                numericLevel = Integer.parseInt((String) rawLevel);
            } catch (NumberFormatException e) {
                LOG.debug("Invalid message level " + rawLevel, e);
                numericLevel = null;
            }

            if (numericLevel == null) {
                level = null;
            } else {
                level = extractLevel(numericLevel);
            }
        } else {
            LOG.debug("Invalid message level {}", rawLevel);
            level = null;
        }

        return level;
    }

    @Nullable
    private GelfMessageLevel extractLevel(int numericLevel) {
        GelfMessageLevel level;
        try {
            level = GelfMessageLevel.fromNumericLevel(numericLevel);
        } catch (IllegalArgumentException e) {
            LOG.debug("Invalid numeric message level " + numericLevel, e);
            level = null;
        }
        return level;
    }

    // TODO: Rework this?
    protected Map<String, Object> toGELFMessage(final Message message) {
        final DateTime timestamp;
        final Object fieldTimeStamp = message.getField(Message.FIELD_TIMESTAMP);
        if (fieldTimeStamp instanceof DateTime) {
            timestamp = (DateTime) fieldTimeStamp;
        } else {
            timestamp = Tools.nowUTC();
        }

        final GelfMessageLevel messageLevel = extractLevel(message.getField(Message.FIELD_LEVEL));
        final String fullMessage = (String) message.getField(Message.FIELD_FULL_MESSAGE);

        final GelfMessageBuilder builder = new GelfMessageBuilder(message.getMessage(), message.getSource())
                .timestamp(timestamp.getMillis() / 1000.0d)
                .additionalFields(message.getFields());

        if (messageLevel != null) {
            builder.level(messageLevel);
        }

        if (fullMessage != null) {
            builder.fullMessage(fullMessage);
        }

        GelfMessage gelfMessage = builder.build();

        // TODO clean this up. It's probably somewhat redundant.

        Map<String, Object> fields = Maps.newHashMap(); // not using ImmutableMap, because values can be NULL.
        fields.put("short_message", gelfMessage.getMessage());
        fields.put("full_message", gelfMessage.getFullMessage());
        fields.put("host", gelfMessage.getHost());
        fields.put("level", gelfMessage.getLevel().getNumericLevel());
        fields.put("timestamp", gelfMessage.getTimestamp());
        fields.put("version", gelfMessage.getVersion().toString());

        for (Map.Entry<String, Object> x : gelfMessage.getAdditionalFields().entrySet()) {
            fields.put("_" + x.getKey(), x.getValue());
        }

        // Clean up unused fields.
        fields.remove("_timestamp");
        fields.remove("__id");

        return fields;
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

    // From okhttp example: https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/RequestBodyCompression.java
    static class GzipRequestInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
                return chain.proceed(originalRequest);
            }

            Request compressedRequest = originalRequest.newBuilder()
                                                       .header(CONTENT_ENCODING, GZIP_ENCODING)
                                                       .method(originalRequest.method(), gzip(originalRequest.body()))
                                                       .build();
            return chain.proceed(compressedRequest);
        }

        private RequestBody gzip(final RequestBody body) {
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return body.contentType();
                }

                @Override
                public long contentLength() {
                    return -1; // We don't know the compressed length in advance!
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                    body.writeTo(gzipSink);
                    gzipSink.close();
                }
            };
        }
    }

}
