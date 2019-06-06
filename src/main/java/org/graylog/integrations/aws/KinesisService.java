package org.graylog.integrations.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.service.AWSLogMessage;
import org.graylog.integrations.aws.service.AWSService;
import org.graylog2.plugin.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.ListStreamsRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;
import software.amazon.awssdk.services.kinesis.model.Record;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class KinesisService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);
    private static final int KINESIS_LIST_STREAMS_MAX_ATTEMPTS = 1000;
    private static final int KINESIS_LIST_STREAMS_LIMIT = 30;
    public static final int EIGHT_BITS = 8;

    private final KinesisClientBuilder kinesisClientBuilder;
    private ObjectMapper objectMapper;

    @Inject
    public KinesisService(KinesisClientBuilder kinesisClientBuilder,
                          ObjectMapper objectMapper) {

        this.kinesisClientBuilder = kinesisClientBuilder;
        this.objectMapper = objectMapper;
    }

    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest request) throws ExecutionException, IOException {

        LOG.info("Conducting healthCheck");

        // List all streams and make sure the indicated stream is in the list.
        List<String> kinesisStreams = getKinesisStreams(request.region(), null, null);

        boolean streamExists = kinesisStreams.stream()
                                             .anyMatch(streamName -> streamName.equals(request.streamName()));
        if (!streamExists) {
            return KinesisHealthCheckResponse.create(false,
                                                     AWSLogMessage.Type.UNKNOWN.toString(),
                                                     "The stream does not exist."); // TODO: Include specific error message here.
        }

        List<Record> records = readKinesisRecords(request);
        if (records.size() == 0) {
            return KinesisHealthCheckResponse.create(false,
                                                     AWSLogMessage.Type.UNKNOWN.toString(),
                                                     ":( The Kinesis stream does not contain any messages yet");
        }

        // Convert the message to a string
        // TODO: Inspect message payload here. If GZipped, then extract and inspect contents.
        // GZipped payloads are likely from Cloud Watch.
        byte[] payloadBytes = records.get(0).data().asByteArray();

        if (isCompressed(payloadBytes)) {
            // Parse as CloudWatch
            final byte[] bytes = Tools.decompressGzip(payloadBytes).getBytes();

            // Extract messages, so that they can be committed to journal one by one.
            final CloudWatchLogSubscriptionData data = objectMapper.readValue(bytes, CloudWatchLogSubscriptionData.class);

            // Pick out the first log entry.
            Optional<CloudWatchLogEntry> logEntry =
                    data.logEvents.stream()
                                  .map(le -> new CloudWatchLogEntry(data.logGroup, data.logStream, le.timestamp, le.message)).findAny();

            // TODO: Add error checking here for optional. If no messages were returned, then return a respond accordingly.
            return detectMessage(logEntry.get().message);
        }

        // The log message is in plain text. Go ahead and parse it straight up.
        return detectMessage(new String(payloadBytes));
    }

    /**
     * Detect the message type
     *
     * @param logMessage A string containing the actual log message.
     * @return a fully built {@code KinesisHealthCheckResponse}.
     */
    private KinesisHealthCheckResponse detectMessage(String logMessage) {

        AWSLogMessage awsLogMessage = new AWSLogMessage(logMessage);

        AWSLogMessage.Type type = awsLogMessage.detectLogMessageType();

        // Build the specific response type for the message.
        String responseMessage = String.format("Success! The message is an %s!", type.getDescription());
        if (type.isUnknown()) {
            responseMessage = "The message is of an unknown type";
        }

        return KinesisHealthCheckResponse.create(true,
                                                 awsLogMessage.detectLogMessageType().toString(),
                                                 responseMessage);
    }

    /**
     * Read the first or last records from the stream.
     *
     * @param request The request details including the region and stream name.
     * @return
     */
    private List<Record> readKinesisRecords(KinesisHealthCheckRequest request) {

        // Mock up Kinesis CloudWatch subscription record.
        // TODO: This will be substituted with actual Kinesis record retrieval later.
        String messageData = "{\n" +
                             "  \"messageType\": \"DATA_MESSAGE\",\n" +
                             "  \"owner\": \"459220251735\",\n" +
                             "  \"logGroup\": \"test-flowlogs\",\n" +
                             "  \"logStream\": \"eni-3423-all\",\n" +
                             "  \"subscriptionFilters\": [\n" +
                             "    \"filter\"\n" +
                             "  ],\n" +
                             "  \"logEvents\": [\n" +
                             "    {\n" +
                             "      \"id\": \"3423\",\n" +
                             "      \"timestamp\": 1559738144000,\n" +
                             "      \"message\": \"2 423432432432 eni-3244234 172.1.1.2 172.1.1.2 80 2264 6 1 52 1559738144 1559738204 ACCEPT OK\"\n" +
                             "    },\n" +
                             "    {\n" +
                             "      \"id\": \"3423\",\n" +
                             "      \"timestamp\": 1559738144000,\n" +
                             "      \"message\": \"2 423432432432 eni-3244234 172.1.1.2 172.1.1.2 80 2264 6 1 52 1559738144 1559738204 ACCEPT OK\"\n" +
                             "    }\n" +
                             "  ]\n" +
                             "}";

        try {
            // Compress the test record, as CloudWatch subscriptions are compressed.
            ByteArrayOutputStream bos = new ByteArrayOutputStream(messageData.getBytes().length);
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(messageData.getBytes());
            gzip.close();
            byte[] compressed = bos.toByteArray();
            bos.close();

            Record record = Record.builder().data(SdkBytes.fromByteArray(compressed)).build();
            return Lists.newArrayList(record);
        } catch (Exception e) {
            LOG.error("Failed to mock up Kinesis record", e);
        }

        return new ArrayList<>();
    }

    /**
     * Checks if the supplied stream is GZip compressed.
     *
     * @param bytes a byte array.
     * @return true if the byte array is GZip compressed and false if not.
     */
    public boolean isCompressed(byte[] bytes) {
        if ((bytes == null) || (bytes.length < 2)) {
            return false;
        } else {

            // If the byte array is GZipped, then the first or second byte will be the GZip magic number.
            boolean firstByteIsMagicNumber = bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC);
            boolean secondByteIsMagicNumber = bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> EIGHT_BITS); // The >> operator shifts the GZIP magic number to the second byte.
            return firstByteIsMagicNumber && secondByteIsMagicNumber;
        }
    }

    /**
     * Get a list of Kinesis stream names. All available streams will be returned.
     *
     * @param regionName The AWS region to query Kinesis stream names from.
     * @return A list of all available Kinesis streams in the supplied region.
     */
    public List<String> getKinesisStreams(String regionName, String accessKeyId, String secretAccessKey) throws ExecutionException {

        LOG.debug("List Kinesis streams for region [{}]", regionName);

        // Only explicitly provide credentials if key/secret are provided.
        // TODO: Remove this IF check and always provided the string credentials. This will prevent the AWS SDK
        //  from reading credentials from environment variables, which we definitely do not want to do.
        if (StringUtils.isNotBlank(accessKeyId) && StringUtils.isNotBlank(secretAccessKey)) {
            StaticCredentialsProvider credentialsProvider =
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
            kinesisClientBuilder.credentialsProvider(credentialsProvider);
        }

        final KinesisClient kinesisClient =
                KinesisClient.builder().region(Region.of(regionName)).build();

        // KinesisClient.listStreams() is paginated. Use a retryer to loop and stream names (while ListStreamsResponse.hasMoreStreams() is true).
        // The stopAfterAttempt retryer option is an emergency brake to prevent infinite loops
        // if AWS API always returns true for hasMoreStreamNames.
        final Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(b -> Objects.equals(b, Boolean.TRUE))
                .withStopStrategy(StopStrategies.stopAfterAttempt(KINESIS_LIST_STREAMS_MAX_ATTEMPTS))
                .build();

        ListStreamsRequest streamsRequest = ListStreamsRequest.builder().limit(KINESIS_LIST_STREAMS_LIMIT).build();
        final ListStreamsResponse listStreamsResponse = kinesisClient.listStreams(streamsRequest);
        final List<String> streamNames = new ArrayList<>(listStreamsResponse.streamNames());

        if (listStreamsResponse.hasMoreStreams()) {
            try {
                retryer.call(() -> {
                    final String lastStreamName = streamNames.get(streamNames.size() - 1);
                    final ListStreamsRequest moreStreamsRequest = ListStreamsRequest.builder()
                                                                                    .exclusiveStartStreamName(lastStreamName)
                                                                                    .limit(KINESIS_LIST_STREAMS_LIMIT).build();
                    final ListStreamsResponse moreSteamsResponse = kinesisClient.listStreams(moreStreamsRequest);
                    streamNames.addAll(moreSteamsResponse.streamNames());

                    // If more streams, then this will execute again.
                    return moreSteamsResponse.hasMoreStreams();
                });
                // Only catch the RetryException, which occurs after too many attempts. When this happens, we still want
                // to the return the response with any streams obtained.
                // All other exceptions will be bubbled up to the client caller.
            } catch (RetryException e) {
                LOG.error("Failed to get all stream names after {} attempts. Proceeding to return currently obtained streams.", KINESIS_LIST_STREAMS_MAX_ATTEMPTS);
            }
        }

        LOG.debug("Kinesis streams queried: [{}]", streamNames);

        return streamNames;
    }

    // TODO Create Kinesis Stream

    // TODO Subscribe to Kinesis Stream

    // TODO getRecord
}