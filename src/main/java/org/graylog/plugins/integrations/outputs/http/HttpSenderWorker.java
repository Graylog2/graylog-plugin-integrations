package org.graylog.plugins.integrations.outputs.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.joschi.jadconfig.util.Duration;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.AttemptTimeLimiters;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for executing actual HTTP request with retry functionality.
 */
public class HttpSenderWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSenderWorker.class);
    private final OkHttpClient httpClient;
    private final HttpUrl url;
    private final ObjectMapper objectMapper;
    private final List<Map<String, Object>> slice;
    private BatchedHttpProducer producer;

    private static final int RETRY_TIME_MS = 2000;
    private static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";
    private static final String GRAYLOG_OUTPUT_USER_AGENT = "graylog-output-gelf-http";
    private static final String GZIP_CONTENT_ENCODING = "Content-Encoding";
    private static final String GZIP_ENCODING = "gzip";
    private static final String USER_AGENT_HEADER = "User-Agent";

    // TODO: Expose retry parameters as config options.
    public static final int MAX_RETRY_ATTEMPTS = 5;
    // TODO: This retryer will be created and destroyed with each thread. Will this produce a performance bottle neck?
    // TODO:
    private static final Retryer<Response> HTTP_RETYER = RetryerBuilder.<Response>newBuilder()
            .retryIfException(t -> t instanceof IOException)
            .retryIfResult( r -> !r.isSuccessful() )
            .withWaitStrategy(WaitStrategies.noWait()) // TODO: Consider using exponential decay here. What impact does this have on serial processing?
            .withStopStrategy(StopStrategies.stopAfterAttempt(MAX_RETRY_ATTEMPTS))
            .withRetryListener(new RetryListener() {
                @Override
                public <V> void onRetry(Attempt<V> attempt) {

                    LOG.error("Trying HTTP request again.");
                    if (attempt.hasException()) {
                        LOG.error("Caught exception during HTTP request: {}, retrying (attempt #{}).", attempt.getExceptionCause(), attempt.getAttemptNumber());
                    }
                    else if (attempt.getAttemptNumber() > 1) {
                        LOG.info("HTTP request finally successful (attempt #{}).", attempt.getAttemptNumber());
                    }
                }
            })
            .build();

    HttpSenderWorker(HttpUrl url, boolean enableGzip, long connectTimeout, long readTimeout, long writeTimeout,
                     final List<Map<String, Object>> slice, BatchedHttpProducer producer) {

        this.producer = producer;
        this.objectMapper = new ObjectMapper(); // Not using injected OM because we need specific (default) settings.
        this.url = url;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(writeTimeout, TimeUnit.MILLISECONDS);

        if (enableGzip) {
            builder.addInterceptor(new GzipRequestInterceptor());
        }
        this.httpClient = builder.build();
        this.slice = slice;
    }

    @Override
    public void run() {

        LOG.trace("[Starting]");

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
                try {
                    response = HTTP_RETYER.call(() -> httpClient.newCall(request).execute());
                } catch (ExecutionException | RetryException e) {
                    if (e instanceof RetryException) {
                        LOG.error("Could not execute HTTP request. Giving up after {} attempts.", ((RetryException) e).getNumberOfFailedAttempts());
                    } else {
                        LOG.error("Could not execute HTTP request.");
                    }
                }


                if (response.isSuccessful()) {
                    LOG.debug("GELF messages written successfully with HTTP response code [{}].", response.code());
                    break;
                } else {
                    LOG.warn("Could not write GELF messages to [{}]. Received HTTP response code [{}]. Will retry.", url.toString(), response.code());
                    throw new IOException();
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
        LOG.trace("[Finished]");
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
                                                       .header(GZIP_CONTENT_ENCODING, GZIP_ENCODING)
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

    public class BatchedRequest {

        public BatchedRequest(List<Map<String, Object>> messages) {
            this.messages = messages;
        }

        public List<Map<String, Object>> messages;
    }
}

