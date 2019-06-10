package org.graylog.integrations.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang3.StringUtils;
import org.graylog.integrations.aws.cloudwatch.CloudWatchLogEntry;
import org.graylog.integrations.aws.cloudwatch.CloudWatchLogSubscriptionData;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.service.AWSLogMessage;
import org.graylog.integrations.aws.service.AWSService;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.graylog2.plugin.journal.RawMessage;
import org.joda.time.DateTime;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class KinesisService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);
    private static final int KINESIS_LIST_STREAMS_MAX_ATTEMPTS = 1000;
    private static final int KINESIS_LIST_STREAMS_LIMIT = 30;
    private static final int EIGHT_BITS = 8;

    private Configuration configuration;
    private final KinesisClientBuilder kinesisClientBuilder;
    private final ObjectMapper objectMapper;
    private final Map<String, Codec.Factory<? extends Codec>> availableCodecs;

    @Inject
    public KinesisService(@Assisted Configuration configuration,
                          KinesisClientBuilder kinesisClientBuilder,
                          ObjectMapper objectMapper,
                          Map<String, Codec.Factory<? extends Codec>> availableCodecs) {

        this.configuration = configuration;
        this.kinesisClientBuilder = kinesisClientBuilder;
        this.objectMapper = objectMapper;
        this.availableCodecs = availableCodecs;
    }

    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest request) throws ExecutionException, IOException {

        LOG.info("Conducting healthCheck");

        // List all streams and make sure the indicated stream is in the list.
        final List<String> kinesisStreams = getKinesisStreams(request.region(), null, null);

        final boolean streamExists = kinesisStreams.stream()
                                                   .anyMatch(streamName -> streamName.equals(request.streamName()));
        if (!streamExists) {
            return KinesisHealthCheckResponse.create(false,
                                                     AWSLogMessage.Type.UNKNOWN.toString(),
                                                     "The stream does not exist.", ""); // TODO: Include specific error message here.
        }

        final List<Record> records = readKinesisRecords(request);
        if (records.size() == 0) {
            return KinesisHealthCheckResponse.create(false,
                                                     AWSLogMessage.Type.UNKNOWN.toString(),
                                                     ":( The Kinesis stream does not contain any messages yet", "");
        }

        // Convert the message to a string
        // GZipped payloads are likely from Cloud Watch.
        final byte[] payloadBytes = records.get(0).data().asByteArray();
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
            // TODO: Identify log group name and pass in here if possible? This would come from the request.
            return detectMessage(logEntry.get().message, request.streamName(), "");
        }

        // The log message is in plain text. Go ahead and parse it straight up.
        // TODO: Identify log group name and pass in here if possible? This would come from the request.
        return detectMessage(new String(payloadBytes), request.streamName(), "");
    }

    /**
     * Detect the message type
     *
     * @param logMessage   A string containing the actual log message.
     * @param streamName   The stream name.
     * @param logGroupName The log group name.
     * @return a fully built {@code KinesisHealthCheckResponse}.
     */
    private KinesisHealthCheckResponse detectMessage(String logMessage, String streamName, String logGroupName) {

        final AWSLogMessage awsLogMessage = new AWSLogMessage(logMessage);

        final AWSLogMessage.Type type = awsLogMessage.detectLogMessageType();

        // Build the specific response type for the message.
        String responseMessage = String.format("Success! The message is an %s!", type.getDescription());

        if (type == AWSLogMessage.Type.FLOW_LOGS) {

            // Parse the Flow Log message
            final CloudWatchLogEntry logEvent = new CloudWatchLogEntry(streamName, logGroupName, DateTime.now().getMillis() / 1000, logMessage);

            // Look up the codec by name
            final Codec.Factory<? extends Codec> codecFactory = this.availableCodecs.get(type.getCodecName());
            if (codecFactory == null) {
                LOG.error("A codec with name [{}] could not be found.", type.getCodecName());
                return null;
            }

            // Parse the message with the selected codec.
            // TODO: Is this codec the correct one to supply?
            final Codec codec = codecFactory.create(configuration);

            // Load up Flow Log codec, parse the message and convert it to GELF JSON
            try {
                final Message fullyParsedMessage = codec.decode(new RawMessage(objectMapper.writeValueAsBytes(logEvent)));

                // TODO: Using ObjectMapper here creates a very verbose message. Consider shortening up the message.
                //  The goal is only to provide the user with a idea of what the parsed message fields look like.
                return KinesisHealthCheckResponse.create(true, awsLogMessage.detectLogMessageType().toString(),
                                                         responseMessage,
                                                         fullyParsedMessage.toString());
            } catch (JsonProcessingException e) {
                LOG.error("An error occurred decoding Flow Log message.", e);
                // TODO: Return appropriate error message here.
            }
        } else if (type.isUnknown()) {
            responseMessage = "The message is of an unknown type";
        }

        return KinesisHealthCheckResponse.create(true,
                                                 awsLogMessage.detectLogMessageType().toString(),
                                                 responseMessage, "");
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
        final String messageData = "{\n" +
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
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(messageData.getBytes().length);
            final GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(messageData.getBytes());
            gzip.close();
            final byte[] compressed = bos.toByteArray();
            bos.close();

            final Record record = Record.builder().data(SdkBytes.fromByteArray(compressed)).build();
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
            final boolean firstByteIsMagicNumber = bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC);
            final boolean secondByteIsMagicNumber = bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> EIGHT_BITS); // The >> operator shifts the GZIP magic number to the second byte.
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
            final StaticCredentialsProvider credentialsProvider =
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
            kinesisClientBuilder.credentialsProvider(credentialsProvider);
        }

        final KinesisClient kinesisClient =
                kinesisClientBuilder.region(Region.of(regionName)).build();

        // KinesisClient.listStreams() is paginated. Use a retryer to loop and stream names (while ListStreamsResponse.hasMoreStreams() is true).
        // The stopAfterAttempt retryer option is an emergency brake to prevent infinite loops
        // if AWS API always returns true for hasMoreStreamNames.
        final Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(b -> Objects.equals(b, Boolean.TRUE))
                .withStopStrategy(StopStrategies.stopAfterAttempt(KINESIS_LIST_STREAMS_MAX_ATTEMPTS))
                .build();

        final ListStreamsRequest streamsRequest = ListStreamsRequest.builder().limit(KINESIS_LIST_STREAMS_LIMIT).build();
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