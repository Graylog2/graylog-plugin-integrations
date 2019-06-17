package org.graylog.integrations.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsResponse;
import software.amazon.awssdk.services.kinesis.model.ListStreamsRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

public class KinesisService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);

    private static final int KINESIS_LIST_STREAMS_MAX_ATTEMPTS = 1000;
    private static final int KINESIS_LIST_STREAMS_LIMIT = 30;
    private static final int EIGHT_BITS = 8;

    private final KinesisClientBuilder kinesisClientBuilder;
    private final ObjectMapper objectMapper;
    private final Map<String, Codec.Factory<? extends Codec>> availableCodecs;

    @Inject
    public KinesisService(KinesisClientBuilder kinesisClientBuilder,
                          ObjectMapper objectMapper,
                          Map<String, Codec.Factory<? extends Codec>> availableCodecs) {

        this.kinesisClientBuilder = kinesisClientBuilder;
        this.objectMapper = objectMapper;
        this.availableCodecs = availableCodecs;
    }

    public KinesisClient createClient(String regionName, String accessKeyId, String secretAccessKey) {

        return kinesisClientBuilder.region(Region.of(regionName))
                .credentialsProvider(AWSService.validateCredentials(accessKeyId, secretAccessKey))
                .build();
    }

    /**
     * The Health Check performs the following actions:
     * <p>
     * 1) Check if the supplied stream exists.
     * 2) Retrieve a log message from the indicated Kinesis stream.
     * 3) Detect the type of log message.
     * 4) Parse the message if it's a known type of message.
     *
     * @param request The request, which indicates which stream region to health check
     * @return a {@code KinesisHealthCheckResponse}, which indicates the type of detected message and a sample parsed
     * message.
     */
    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest request) throws ExecutionException, IOException {
        KinesisClient kinesisClient =
                createClient(request.streamName(), request.awsAccessKeyId(), request.awsSecretAccessKey());

        LOG.debug("Executing healthCheck");
        LOG.debug("Requesting a list of streams to find out if the indicated stream exists.");

        // List all streams and make sure the indicated stream is in the list.
        final List<String> kinesisStreams = getKinesisStreams(request.region(),
                                                              request.awsAccessKeyId(),
                                                              request.awsSecretAccessKey());

        final boolean streamExists = kinesisStreams.stream()
                .anyMatch(streamName -> streamName.equals(request.streamName()));
        if (!streamExists) {
            String explanation = "The requested stream was not found.";
            LOG.error(explanation);
            return KinesisHealthCheckResponse.create(false,
                                                     AWSLogMessage.Type.UNKNOWN.toString(),
                                                     explanation, request.logGroupName());
        }

        LOG.debug("The stream [{}] exists", request.streamName());

        final List<Record> records = retrieveRecords(request.streamName(), kinesisClient);
        if (records.size() == 0) {
            String explanation = "The Kinesis stream does not contain any messages.";
            LOG.error(explanation);
            return KinesisHealthCheckResponse.create(false,
                                                     AWSLogMessage.Type.UNKNOWN.toString(),
                                                     explanation, request.logGroupName());
        }

        // Handle compressed/GZipped payloads. These are likely from Cloud Watch.
        final byte[] payloadBytes = records.get(0).data().asByteArray();
        if (isCompressed(payloadBytes)) {

            return handleCompressedMessages(request, payloadBytes);
        }

        // Fall through handles all non-compressed payloads.
        // The log message is in plain text. Go ahead and parse it straight up.
        return detectMessage(new String(payloadBytes), request.streamName(), request.logGroupName());
    }

    /**
     * Detect the message type.
     *
     * @param logMessage   A string containing the actual log message.
     * @param streamName   The stream name.
     * @param logGroupName The log group name.
     * @return A {@code KinesisHealthCheckResponse} with the fully parsed message and type.
     */
    private KinesisHealthCheckResponse detectMessage(String logMessage, String streamName, String logGroupName) {

        LOG.debug("Attempting to detect the type of log message. message [{}] stream [{}] log group [{}]",
                  logMessage, streamName, logGroupName);

        final AWSLogMessage awsLogMessage = new AWSLogMessage(logMessage);
        final AWSLogMessage.Type type = awsLogMessage.detectLogMessageType();

        LOG.debug("The message is type [{}]", type);

        // Build the specific default response type for the message. This might be overridden below.
        String responseMessage = String.format("Success! The message is an %s!", type.getDescription());

        // Parse the Flow Log message
        final CloudWatchLogEntry logEvent = CloudWatchLogEntry.create(logGroupName, streamName, DateTime.now().getMillis() / 1000, logMessage);

        // Look up the codec for the type of log by name.
        // All messages will resolve to a particular codec. Event Unknown messages will resolve to the raw logs codec.
        final Codec.Factory<? extends Codec> codecFactory = this.availableCodecs.get(type.getCodecName());
        if (codecFactory == null) {
            String explanation = String.format(Locale.ENGLISH, "A codec with name [%s] could not be found.", type.getCodecName());
            LOG.error(explanation);
            return KinesisHealthCheckResponse.create(false, type.toString(), explanation, null);
        }

        // Parse the message with the selected codec.
        // TODO: Do we need to provide a valid configuration here?
        final Codec codec = codecFactory.create(Configuration.EMPTY_CONFIGURATION);

        // Load up appropriate codec and parse the message.
        final Message fullyParsedMessage;
        try {
            fullyParsedMessage = codec.decode(new RawMessage(objectMapper.writeValueAsBytes(logEvent)));
        } catch (JsonProcessingException e) {
            LOG.error("An error occurred decoding Flow Log message.", e);
            String explanation = String.format(Locale.ENGLISH, "Message decoding failed. More information might be " +
                    "available by enabling Debug logging. message [%s]", logMessage);
            LOG.error(explanation);
            return KinesisHealthCheckResponse.create(false, type.toString(), explanation, null);
        }

        // Message decoding can return null, so check for this.
        if (fullyParsedMessage == null) {
            String explanation = String.format(Locale.ENGLISH, "Message decoding failed. More information might be " +
                    "available by enabling Debug logging. message [%s]", logMessage);
            LOG.error(explanation);
            return KinesisHealthCheckResponse.create(false, type.toString(), explanation, null);
        }

        return KinesisHealthCheckResponse.create(true, awsLogMessage.detectLogMessageType().toString(),
                                                 responseMessage,
                                                 buildMessageSummary(fullyParsedMessage, logEvent.message()));
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
     * Prepare a string summary of all fields. This will be displayed on the Health Check results page.
     * The purpose is to provide the user with a summary of the parsed fields.
     *
     * Note that the {@code org.graylog2.plugin.Message.toString()} method is not suitable for this, since it is a
     * one-line summary. Multi-line is important for clarity.
     *
     * @param message     The fully parsed {@code org.graylog2.plugin.Message} object.
     * @param fullMessage The full, unparsed message string.
     * @return a summary of fields in the following format:
     *
     * full_message: 2 423432432432 eni-3244234 172.1.1.2 172.1.1.2 80 2264 6 1 52 1559738144 1559738204 ACCEPT OK
     * protocol_number: 6
     * src_addr: 172.1.1.2
     * source: aws-flowlogs
     * message: eni-3244234 ACCEPT TCP 172.1.1.2:80 -> 172.1.1.2:2264
     * packets: 1
     * ...
     */
    public String buildMessageSummary(Message message, String fullMessage) {

        // Build up a representation of the message.
        final StringBuilder builder = new StringBuilder();
        final String cleanMessage = fullMessage.replaceAll("\\n", "").replaceAll("\\t", "");

        // Append the entire message.
        builder.append("full_message: ");
        builder.append(StringUtils.abbreviate(cleanMessage, 225)); // Shorten if too long.

        // Append the field values.
        builder.append("\n");
        final Map<String, Object> filteredFields = Maps.newHashMap(message.getFields());
        Joiner.on("\n").withKeyValueSeparator(": ").appendTo(builder, filteredFields);

        return builder.toString();
    }

    /**
     * Get a list of Kinesis stream names. All available streams will be returned.
     *
     * @param regionName The AWS region to query Kinesis stream names from.
     * @return A list of all available Kinesis streams in the supplied region.
     */
    public List<String> getKinesisStreams(String regionName, String accessKeyId, String secretAccessKey) throws ExecutionException {

        LOG.debug("List Kinesis streams for region [{}]", regionName);

        // KinesisClient.listStreams() is paginated. Use a retryer to loop and stream names (while ListStreamsResponse.hasMoreStreams() is true).
        // The stopAfterAttempt retryer option is an emergency brake to prevent infinite loops
        // if AWS API always returns true for hasMoreStreamNames.

        final KinesisClient kinesisClient = createClient(regionName, accessKeyId, secretAccessKey);

        ListStreamsRequest streamsRequest = ListStreamsRequest.builder().limit(KINESIS_LIST_STREAMS_LIMIT).build();
        final ListStreamsResponse listStreamsResponse = kinesisClient.listStreams(streamsRequest);
        final List<String> streamNames = new ArrayList<>(listStreamsResponse.streamNames());

        // Create retryer to keep checking if more streams exist
        final Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(b -> Objects.equals(b, Boolean.TRUE))
                .withStopStrategy(StopStrategies.stopAfterAttempt(KINESIS_LIST_STREAMS_MAX_ATTEMPTS))
                .build();

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


    /**
     * Get a list of Records that exists in a Kinesis stream.
     *
     * @param kinesisStream The name of the Kinesis stream
     * @param kinesisClient The KinesClient interface
     * @return A list of all the records in a Kinesis stream
     */
    public static List<Record> retrieveRecords(String kinesisStream, KinesisClient kinesisClient) {

        // TODO add error logging
        // Create ListShard request and response and designate the Kinesis stream
        ListShardsRequest listShardsRequest = ListShardsRequest.builder().streamName(kinesisStream).build();
        ListShardsResponse listShardsResponse = kinesisClient.listShards(listShardsRequest);

        String shardId;
        int shardNum = listShardsResponse.shards().size();
        List<Record> recordsList = new ArrayList<>();
        // Iterate through the shards that exist
        String nextShardIterator;
        for (int i = 0; i < shardNum; i++) {
            shardId = listShardsResponse.shards().get(i).shardId();
            GetShardIteratorRequest getShardIteratorRequest =
                    GetShardIteratorRequest.builder()
                            .shardId(shardId)
                            .streamName(kinesisStream)
                            .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
                            .build();
            String currentShardIterator = kinesisClient.getShardIterator(getShardIteratorRequest).shardIterator();
            GetRecordsRequest getRecordsRequest = GetRecordsRequest.builder().shardIterator(currentShardIterator).build();
            GetRecordsResponse getRecordsResponse = kinesisClient.getRecords(getRecordsRequest);
            boolean stayOnCurrentShard = true;

            // Loop until shardIterator is current
            while (stayOnCurrentShard) {
                int recordSize = getRecordsResponse.records().size();
                for (int k = 0; k < recordSize; k++) {
                    recordsList.add(getRecordsResponse.records().get(k));
                }
                // Set the nextShardIterator
                nextShardIterator = getRecordsResponse.nextShardIterator();
                getRecordsRequest = GetRecordsRequest.builder().shardIterator(nextShardIterator).build();
                getRecordsResponse = kinesisClient.getRecords(getRecordsRequest);

                // Find when the shardIterator is current
                if (getRecordsResponse.millisBehindLatest() == 0 && recordSize == 0) {
                    stayOnCurrentShard = false;
                }
            }
        }
        return recordsList;
    }
    // TODO Add method for auto-setup with stream creation and subscription.
}