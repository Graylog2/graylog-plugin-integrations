package org.graylog.integrations.aws.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.graylog.integrations.aws.AWSLogMessage;
import org.graylog.integrations.aws.cloudwatch.CloudWatchLogEntry;
import org.graylog.integrations.aws.cloudwatch.CloudWatchLogSubscriptionData;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.HealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.StreamsResponse;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

/**
 * Service for all AWS Kinesis business logic and SDK usages.
 */
public class KinesisService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);

    private static final int EIGHT_BITS = 8;
    private static final int KINESIS_LIST_STREAMS_MAX_ATTEMPTS = 1000;
    private static final int KINESIS_LIST_STREAMS_LIMIT = 30;
    private static final int RECORDS_SAMPLE_SIZE = 10;

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

    private KinesisClient createClient(String regionName, String accessKeyId, String secretAccessKey) {

        return kinesisClientBuilder.region(Region.of(regionName))
                                   .credentialsProvider(AWSService.buildCredentialProvider(accessKeyId, secretAccessKey))
                                   .build();
    }

    /**
     * The Health Check performs the following actions:
     * <p>
     * 1) Get all the Kinesis streams.
     * 2) Check if the supplied stream exists.
     * 3) Retrieve one record from Kinesis stream.
     * 4) Check if the payload is compressed.
     * 5) Detect the type of log message.
     * 6) Parse the message if is of a known type.
     *
     * @param request The request, which indicates which stream region to health check
     * @return a {@code KinesisHealthCheckResponse}, which indicates the type of detected message and a sample parsed
     * message.
     */
    public HealthCheckResponse healthCheck(KinesisHealthCheckRequest request) throws ExecutionException, IOException {

        LOG.debug("Executing healthCheck");
        LOG.debug("Requesting a list of streams to find out if the indicated stream exists.");
        // Get all the Kinesis streams that exist for a user and region
        final StreamsResponse streamsResponse = getKinesisStreamNames(request.region(),
                                                                      request.awsAccessKeyId(),
                                                                      request.awsSecretAccessKey());

        // Check if Kinesis stream exists
        final boolean streamExists = streamsResponse.streams().stream()
                                                    .anyMatch(streamName -> streamName.equals(request.streamName()));
        if (!streamExists) {
            String explanation = String.format("The requested stream [%s] was not found.", request.streamName());
            LOG.error(explanation);
            return HealthCheckResponse.create(false,
                                              AWSLogMessage.Type.UNKNOWN.toString(),
                                              explanation, request.logGroupName());
        }

        LOG.debug("The stream [{}] exists", request.streamName());

        KinesisClient kinesisClient =
                createClient(request.region(), request.awsAccessKeyId(), request.awsSecretAccessKey());

        // Retrieve one records from the Kinesis stream
        final List<Record> records = retrieveRecords(request.streamName(), kinesisClient);
        if (records.size() == 0) {
            String explanation = "The Kinesis stream does not contain any messages.";
            LOG.error(explanation);
            return HealthCheckResponse.create(false,
                                              AWSLogMessage.Type.UNKNOWN.toString(),
                                              explanation, request.logGroupName());
        }

        // Select random record from list, and check if the payload is compressed
        final byte[] payloadBytes = selectRandomRecord(records).data().asByteArray();
        if (isCompressed(payloadBytes)) {
            return handleCompressedMessages(request, payloadBytes);
        }

        // Detect the type of message
        return detectAndParseMessage(new String(payloadBytes), request.streamName(), request.logGroupName());
    }

    /**
     * Get a list of Kinesis stream names. All available streams will be returned.
     *
     * @param regionName The AWS region to query Kinesis stream names from.
     * @return A list of all available Kinesis streams in the supplied region.
     */
    public StreamsResponse getKinesisStreamNames(String regionName, String accessKeyId, String secretAccessKey) throws ExecutionException {

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
        return StreamsResponse.create(streamNames, streamNames.size());
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
            // The >> operator shifts the GZIP magic number to the second byte.
            final boolean secondByteIsMagicNumber = bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> EIGHT_BITS);
            return firstByteIsMagicNumber && secondByteIsMagicNumber;
        }
    }

    /**
     * CloudWatch Kinesis subscription payloads are always compressed. Detecting a compressed payload is currently
     * how the Health Check identifies that the payload has been sent from CloudWatch.
     *
     * @param request      The Health Check request.
     * @param payloadBytes The raw compressed binary payload from Kinesis.
     * @return a {@code KinesisHealthCheckResponse}, which indicates the type of detected message and a sample parsed
     * message.
     * @see <a href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/SubscriptionFilters.html"/>
     */
    private HealthCheckResponse handleCompressedMessages(KinesisHealthCheckRequest request, byte[] payloadBytes) throws IOException {
        LOG.debug("The supplied payload is GZip compressed. Proceeding to decompress.");

        final byte[] bytes = Tools.decompressGzip(payloadBytes).getBytes();
        LOG.debug("They payload was decompressed successfully. size [{}]", bytes.length);

        // Assume that the payload is from CloudWatch.
        // Extract messages, so that they can be committed to journal one by one.
        final CloudWatchLogSubscriptionData data = objectMapper.readValue(bytes, CloudWatchLogSubscriptionData.class);

        if (LOG.isTraceEnabled()) {
            // Log the number of events retrieved from CloudWatch. DO NOT log the content of the messages.
            LOG.trace("[{}] messages obtained from CloudWatch", data.logEvents().size());
        }

        // Pick just one log entry.
        Optional<CloudWatchLogEntry> logEntryOptional =
                data.logEvents().stream()
                    .map(le -> CloudWatchLogEntry.create(data.logGroup(), data.logStream(), le.timestamp(), le.message())).findAny();

        if (!logEntryOptional.isPresent()) {
            LOG.debug("One log messages was successfully selected from the CloudWatch payload.");
            return HealthCheckResponse.create(false,
                                              AWSLogMessage.Type.UNKNOWN.toString(),
                                              "The Kinesis stream does not contain any messages.", request.logGroupName());
        }

        return detectAndParseMessage(logEntryOptional.get().message(), request.streamName(), request.logGroupName());
    }

    /**
     * Get a list of Records that exists in a Kinesis stream.
     *
     * @param kinesisStream The name of the Kinesis stream
     * @param kinesisClient The KinesClient interface
     * @return A sample size of records (between 0-5 records) in a Kinesis stream
     */
    List<Record> retrieveRecords(String kinesisStream, KinesisClient kinesisClient) {

        LOG.debug("About to retrieve logs records from Kinesis.");
        // Create ListShard request and response and designate the Kinesis stream
        final ListShardsRequest listShardsRequest = ListShardsRequest.builder().streamName(kinesisStream).build();
        final ListShardsResponse listShardsResponse = kinesisClient.listShards(listShardsRequest);
        final List<Record> recordsList = new ArrayList<>();

        // Iterate through the shards that exist
        for (int i = 0; i < listShardsResponse.shards().size(); i++) {
            final String shardId = listShardsResponse.shards().get(i).shardId();
            final GetShardIteratorRequest getShardIteratorRequest =
                    GetShardIteratorRequest.builder()
                                           .shardId(shardId)
                                           .streamName(kinesisStream)
                                           .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
                                           .build();
            String shardIterator = kinesisClient.getShardIterator(getShardIteratorRequest).shardIterator();
            boolean stayOnCurrentShard = true;
            LOG.debug("Retrieved shard id: [{}] with shard iterator: [{}]", shardId, shardIterator);
            // Loop until shardIterator is current
            while (stayOnCurrentShard) {
                // Set the nextShardIterator
                LOG.debug("Getting more records");
                final GetRecordsRequest getRecordsRequest = GetRecordsRequest.builder().shardIterator(shardIterator).build();
                final GetRecordsResponse getRecordsResponse = kinesisClient.getRecords(getRecordsRequest);
                shardIterator = getRecordsResponse.nextShardIterator();

                final int recordSize = getRecordsResponse.records().size();
                for (int k = 0; k < recordSize; k++) {
                    recordsList.add(getRecordsResponse.records().get(k));
                    // Return as soon as sample size is met
                    if (recordsList.size() == RECORDS_SAMPLE_SIZE) {
                        LOG.debug("Returning the list of records now that sample size [{}] has been met.", RECORDS_SAMPLE_SIZE);
                        return recordsList;
                    }
                }

                // Find when the shardIterator is current
                if (getRecordsResponse.millisBehindLatest() == 0) {
                    LOG.debug("Found the end of the shard. No more records returned from the shard.");
                    stayOnCurrentShard = false;
                }
            }
        }
        LOG.debug("Returning the list with [{}] records.", recordsList.size());
        return recordsList;
    }

    /**
     * Detect the message type.
     *
     * @param logMessage   A string containing the actual log message.
     * @param streamName   The stream name.
     * @param logGroupName The log group name.
     * @return A {@code KinesisHealthCheckResponse} with the fully parsed message and type.
     */
    private HealthCheckResponse detectAndParseMessage(String logMessage, String streamName, String logGroupName) {

        LOG.debug("Attempting to detect the type of log message. message [{}] stream [{}] log group [{}].",
                  logMessage, streamName, logGroupName);

        final AWSLogMessage awsLogMessage = new AWSLogMessage(logMessage);
        final AWSLogMessage.Type type = awsLogMessage.detectLogMessageType();

        LOG.debug("The message is type [{}]", type);

        // Build the specific default response type for the message. This might be overridden below.
        final String responseMessage = String.format("Success. The message is an %s.", type.getDescription());

        // Parse the Flow Log message
        final CloudWatchLogEntry logEvent = CloudWatchLogEntry.create(logGroupName, streamName, DateTime.now().getMillis() / 1000, logMessage);

        // Detect the codec needed for the type of log by name.
        // All messages will resolve to a particular codec. Event Unknown messages will resolve to the raw logs codec.
        final Codec.Factory<? extends Codec> codecFactory = this.availableCodecs.get(type.getCodecName());
        if (codecFactory == null) {
            final String explanation = String.format("A codec with name [%s] could not be found.", type.getCodecName());
            LOG.error(explanation);
            return HealthCheckResponse.create(false, type.toString(), explanation, null);
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
            final String explanation = String.format("Message decoding failed. More information might be " +
                                                     "available by enabling Debug logging. message [%s]", logMessage);
            LOG.error(explanation);
            return HealthCheckResponse.create(false, type.toString(), explanation, null);
        }

        // Check if parsing message returns null.
        if (fullyParsedMessage == null) {
            final String explanation = String.format("Message decoding failed. More information might be " +
                                                     "available by enabling Debug logging. message [%s]", logMessage);
            LOG.error(explanation);
            return HealthCheckResponse.create(false, type.toString(), explanation, null);
        }

        LOG.debug("Successfully parsed message type [{}] with codec [{}].", type, type.getCodecName());

        return HealthCheckResponse.create(true, awsLogMessage.detectLogMessageType().toString(),
                                          responseMessage,
                                          buildMessageSummary(fullyParsedMessage, logEvent.message()));
    }

    /**
     * Prepare a string summary of all fields. This will be displayed on the Health Check results page.
     * The purpose is to provide the user with a summary of the parsed fields.
     * <p>
     * Note that the {@code org.graylog2.plugin.Message.toString()} method is not suitable for this, since it is a
     * one-line summary. Multi-line is important for clarity.
     *
     * @param message     The fully parsed {@code org.graylog2.plugin.Message} object.
     * @param fullMessage The full, unparsed message string.
     * @return a summary of fields in the following format:
     * <p>
     * full_message: 2 423432432432 eni-3244234 172.1.1.2 172.1.1.2 80 2264 6 1 52 1559738144 1559738204 ACCEPT OK
     * protocol_number: 6
     * src_addr: 172.1.1.2
     * source: aws-flowlogs
     * message: eni-3244234 ACCEPT TCP 172.1.1.2:80 -> 172.1.1.2:2264
     * packets: 1
     * ...
     */
    String buildMessageSummary(Message message, String fullMessage) {

        // Build up a multi-line string representation of the message.
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

    Record selectRandomRecord(List<Record> recordsList) {

        Preconditions.checkArgument(CollectionUtils.isNotEmpty(recordsList), "Records list can not be empty.");

        LOG.debug("Selecting a random Record from the sample list.");
        return recordsList.get(new Random().nextInt(recordsList.size()));
    }
}