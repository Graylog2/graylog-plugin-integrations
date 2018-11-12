package org.graylog.plugins.integrations.outputs.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpSenderThread implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSenderThread.class);
    private final OkHttpClient httpClient;
    private final HttpUrl url;
    private final ObjectMapper objectMapper;
    private final List<Map<String, Object>> slice;
    private BatchedHttpProducer producer;

    private static final int RETRY_TIME_MS = 2000;
    private static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";
    private static final String GRAYLOG_OUTPUT_USER_AGENT = "graylog-output-gelf-http";
    private static final String USER_AGENT_HEADER = "User-Agent";

    HttpSenderThread(HttpUrl url, boolean enableGzip, long connectTimeout, long readTimeout, long writeTimeout,
                            final List<Map<String, Object>> slice, BatchedHttpProducer producer) {

        this.producer = producer;
        this.objectMapper = new ObjectMapper(); // Not using injected OM because we need specific (default) settings.
        this.url = url;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(writeTimeout, TimeUnit.MILLISECONDS);

        if (enableGzip) {
            builder.addInterceptor(new BatchedHttpProducer.GzipRequestInterceptor());
        }
        this.httpClient = builder.build();
        this.slice = slice;
    }

    @Override
    public void run() {

        producer.incrementThreadCount();

        BatchedRequest batchedRequest = new BatchedRequest(slice);

        // We will retry messages that were not accepted. The OutputBuffer timeout has to be adapted properly, based on the use-case.
        while (true) {
            byte[] body;
            try {
                body = objectMapper.writeValueAsBytes(batchedRequest);
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
        producer.decrementThreadCount();
    }

    public class BatchedRequest {

        public BatchedRequest() { }

        public BatchedRequest(List<Map<String, Object>> messages) {
            this.messages = messages;
        }

        public List<Map<String, Object>> messages;
    }
}

