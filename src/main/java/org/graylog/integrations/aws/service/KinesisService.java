package org.graylog.integrations.aws.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.graylog.integrations.aws.AWSLogMessage;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.cloudwatch.CloudWatchLogEvent;
import org.graylog.integrations.aws.cloudwatch.CloudWatchLogSubscriptionData;
import org.graylog.integrations.aws.cloudwatch.KinesisLogEntry;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.requests.KinesisNewStreamRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.KinesisNewStreamResponse;
import org.graylog.integrations.aws.resources.responses.StreamsResponse;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.graylog2.plugin.journal.RawMessage;
import org.graylog2.shared.utilities.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.CreateStreamResponse;
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
import java.util.HashMap;
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
    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest request) throws ExecutionException, IOException {

        LOG.debug("Executing healthCheck");
        LOG.debug("Requesting a list of streams to find out if the indicated stream exists.");
        // Get all the Kinesis streams that exist for a user and region
        StreamsResponse kinesisStreamNames = getKinesisStreamNames(request.region(),
                                                                   request.awsAccessKeyId(),
                                                                   request.awsSecretAccessKey());

        // Check if Kinesis stream exists
        final boolean streamExists = kinesisStreamNames.streams().stream()
                                                       .anyMatch(streamName -> streamName.equals(request.streamName()));
        if (!streamExists) {
            String explanation = String.format("The requested stream [%s] was not found.", request.streamName());
            LOG.error(explanation);
            return KinesisHealthCheckResponse.createFailed(explanation);
        }

        LOG.debug("The stream [{}] exists", request.streamName());

        KinesisClient kinesisClient =
                createClient(request.region(), request.awsAccessKeyId(), request.awsSecretAccessKey());

        // Retrieve one records from the Kinesis stream
        final List<Record> records = retrieveRecords(request.streamName(), kinesisClient);
        if (records.size() == 0) {
            String explanation = "The Kinesis stream does not contain any messages.";
            LOG.error(explanation);
            return KinesisHealthCheckResponse.createFailed(explanation);
        }

        // Select random record from list, and check if the payload is compressed
        Record record = selectRandomRecord(records);
        final byte[] payloadBytes = record.data().asByteArray();
        if (isCompressed(payloadBytes)) {
            return handleCompressedMessages(request, payloadBytes);
        }

        // The best timestamp available is the approximate arrival time of the message to the Kinesis stream.
        DateTime timestamp = new DateTime(record.approximateArrivalTimestamp().toEpochMilli(), DateTimeZone.UTC);
        return detectAndParseMessage(new String(payloadBytes), timestamp, request.streamName(), "", "");
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
    private KinesisHealthCheckResponse handleCompressedMessages(KinesisHealthCheckRequest request, byte[] payloadBytes) throws IOException {
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
        Optional<CloudWatchLogEvent> logEntryOptional = data.logEvents().stream().findAny();

        if (!logEntryOptional.isPresent()) {
            String message = "The CloudWatch payload did not contain any messages. This should not happen. " +
                             "See https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/SubscriptionFilters.html";
            LOG.debug(message);
            return KinesisHealthCheckResponse.createFailed(message);
        }

        CloudWatchLogEvent logEntry = logEntryOptional.get();
        DateTime timestamp = new DateTime(logEntry.timestamp(), DateTimeZone.UTC);
        return detectAndParseMessage(logEntry.message(), timestamp,
                                     request.streamName(), data.logGroup(), data.logStream());
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
     * @param logMessage        A string containing the actual log message.
     * @param timestamp         The message timestamp.
     * @param kinesisStreamName The stream name.
     * @param logGroupName      The CloudWatch log group name.
     * @param logStreamName     The CloudWatch log stream name.
     * @return A {@code KinesisHealthCheckResponse} with the fully parsed message and type.
     */
    private KinesisHealthCheckResponse detectAndParseMessage(String logMessage, DateTime timestamp, String kinesisStreamName,
                                                             String logGroupName, String logStreamName) {

        LOG.debug("Attempting to detect the type of log message. message [{}] stream [{}] log group [{}].",
                  logMessage, kinesisStreamName, logGroupName);

        final AWSLogMessage awsLogMessage = new AWSLogMessage(logMessage);
        AWSMessageType awsMessageType = awsLogMessage.detectLogMessageType();

        LOG.debug("The message is type [{}]", awsMessageType);

        // Build the specific default response type for the message. This might be overridden below.
        final String responseMessage = String.format("Success. The message is a %s message.", awsMessageType.getLabel());

        // Parse the Flow Log message
        final KinesisLogEntry logEvent = KinesisLogEntry.create(kinesisStreamName, logGroupName, logStreamName,
                                                                timestamp, logMessage);

        // Detect the codec needed for the type of log by name.
        // All messages will resolve to a particular codec. Event Unknown messages will resolve to the raw logs codec.
        final Codec.Factory<? extends Codec> codecFactory = this.availableCodecs.get(awsMessageType.getCodecName());
        if (codecFactory == null) {
            final String explanation = String.format("A codec with name [%s] could not be found.", awsMessageType.getCodecName());
            LOG.error(explanation);
            return KinesisHealthCheckResponse.createFailed(explanation);
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
            return KinesisHealthCheckResponse.createFailed(explanation);
        }

        // Check if parsing message returns null.
        if (fullyParsedMessage == null) {
            final String explanation = String.format("Message decoding failed. More information might be " +
                                                     "available by enabling Debug logging. message [%s]", logMessage);
            LOG.error(explanation);
            return KinesisHealthCheckResponse.createFailed(explanation);
        }

        LOG.debug("Successfully parsed message type [{}] with codec [{}].", awsMessageType, awsMessageType.getCodecName());

        return KinesisHealthCheckResponse.create(true, awsMessageType,
                                                 responseMessage,
                                                 fullyParsedMessage.getFields());
    }

    Record selectRandomRecord(List<Record> recordsList) {

        Preconditions.checkArgument(CollectionUtils.isNotEmpty(recordsList), "Records list can not be empty.");

        LOG.debug("Selecting a random Record from the sample list.");
        return recordsList.get(new Random().nextInt(recordsList.size()));
    }

    /**
     * Creates a new Kinesis stream.
     *
     * @param kinesisNewStreamRequest request which contains region, access, secret, region, streamName and shardCount
     * @return the status response
     */
    public KinesisNewStreamResponse createNewKinesisStream(KinesisNewStreamRequest kinesisNewStreamRequest) {
        // TODO add error handling and logging
        LOG.debug("Creating Kinesis client with the provided credentials.");
        KinesisClient kinesisClient = createClient(kinesisNewStreamRequest.region(),
                                                   kinesisNewStreamRequest.awsAccessKeyId(),
                                                   kinesisNewStreamRequest.awsSecretAccessKey());

        LOG.debug("Creating new Kinesis stream request [{}].", kinesisNewStreamRequest.streamName());
        CreateStreamRequest createStreamRequest = CreateStreamRequest.builder()
                                                                     .streamName(kinesisNewStreamRequest.streamName())
                                                                     .shardCount(kinesisNewStreamRequest.shardCount())
                                                                     .build();
        LOG.debug("Sending request to create new Kinesis stream [{}] with [{}] shards.",
                  kinesisNewStreamRequest.streamName(), kinesisNewStreamRequest.shardCount());

        String responseMessage;
        try {
            CreateStreamResponse streamResponse = kinesisClient.createStream(createStreamRequest);
            responseMessage = String.format("Success. The new stream [%s] was created with [%d] shards.",
                                            kinesisNewStreamRequest.streamName(), kinesisNewStreamRequest.shardCount());
            return KinesisNewStreamResponse.create(true, responseMessage, new HashMap<>());
        } catch (Exception e) {

            String specificError = ExceptionUtils.formatMessageCause(e);
            responseMessage = String.format("Attempt to create new Kinesis stream [%s] " +
                                            "with [%d] failed due to the following exception: [%s]",
                                            kinesisNewStreamRequest.streamName(), kinesisNewStreamRequest.shardCount(),
                                            specificError);
            LOG.debug(responseMessage);
            return KinesisNewStreamResponse.create(false, responseMessage, new HashMap<>());
        }
    }
}