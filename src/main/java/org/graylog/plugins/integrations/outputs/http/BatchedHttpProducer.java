package org.graylog.plugins.integrations.outputs.http;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfMessageLevel;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.Tools;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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

public class BatchedHttpProducer {

    private static final Logger LOG = LoggerFactory.getLogger(BatchedHttpProducer.class);
    private static final String SENDER_THREAD_FORMAT = "gelf-http-output-sender-%d";
    private static final String SCHEDULER_THREAD_FORMAT = "gelf-http-output-batch-scheduler-%d";

    private DateTime lastBatchWrite;
    private final int maximumBatchSize;
    private final Queue<Map<String, Object>> batch;

    private final AtomicBoolean isRunning;

    private ExecutorService executor;
    private final AtomicInteger activeThreads;
    private final boolean enableGZip;
    private final long writeTimeout;
    private final long readTimeout;
    private final long connectTimeout;
    private final int batchTimeout;
    private final int maximumThreads;
    private final HttpUrl url;

    private static final int MILLIS = 5;
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String GZIP_ENCODING = "gzip";

    BatchedHttpProducer(int maximumBatchSize, String url, int batchTimeout, int maximumThreads, boolean enableGZip, long writeTimeout, long readTimeout, long connectTimeout) {

        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.isRunning = new AtomicBoolean();
        this.activeThreads = new AtomicInteger(0);

        this.maximumBatchSize = maximumBatchSize;
        this.batch = new ConcurrentLinkedQueue<>();

        this.url = HttpUrl.parse(url);

        this.batchTimeout = batchTimeout;
        this.maximumThreads = maximumThreads;
        this.enableGZip = enableGZip;
    }

    public void start() {

        LOG.debug("[Starting]");
        this.executor = Executors.newFixedThreadPool(this.maximumThreads,
                                                     new ThreadFactoryBuilder()
                                                             .setDaemon(true)
                                                             .setNameFormat(SENDER_THREAD_FORMAT)
                                                             .build());

        // Schedule writing of batches.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                                                           .setDaemon(true)
                                                           .setNameFormat(SCHEDULER_THREAD_FORMAT)
                                                           .build())
                 .scheduleWithFixedDelay(writeBatchIfReady(), 0, 50, TimeUnit.MILLISECONDS);

        this.isRunning.set(true);

        LOG.debug("[Started successfully]");
    }

    private Runnable writeBatchIfReady() {
        return () -> {

            try {
                // Wait until we have enough messages or the wait timeout has passed.
                long size = batch.size();

                boolean inBatchTimeout = inBatchTimeout();
                LOG.trace("Checking if batch should run. Current batch size [{} out of {}] batch in timeout [{}].", size, maximumBatchSize, inBatchTimeout);
                if (size >= maximumBatchSize || inBatchTimeout) {

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
                        LOG.debug("Slice is empty");
                        return;
                    }

                    LOG.debug("Sending slice of <{}> messages from batch of <{}>.", slice.size(), size);
                    HttpSenderWorker runner = new HttpSenderWorker(url, enableGZip, connectTimeout, readTimeout, writeTimeout,
                                                                   slice, this);
                    executor.submit(runner);
                } else {
                    LOG.trace("Not writing batch: Timeout or batch size not reached yet.");
                }
            } catch (Exception e) {
                LOG.error("Failure when attempting to write batch.", e);
            }
        };
    }

    void writeMessage(Message message) {

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

    // TODO: Rework this?
    private Map<String, Object> toGELFMessage(final Message message) {
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

    public void stop() {
        this.isRunning.set(false);
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    private boolean inBatchTimeout() {
        if (lastBatchWrite == null) {
            LOG.trace("First batch time recording.");
            return true;
        }

        if (lastBatchWrite.isBefore(DateTime.now().minusMillis(batchTimeout))) {
            LOG.debug("Batch timeout reached!");
            return true;
        } else {
            return false;
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

    void decrementThreadCount() {

        activeThreads.decrementAndGet();
    }

    void incrementThreadCount() {

        lastBatchWrite = DateTime.now();
        activeThreads.incrementAndGet();
    }
}