package org.graylog.integrations.aws;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.service.AWSLogMessage;
import org.graylog.integrations.aws.service.AWSService;
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
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class KinesisService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);
    private static final int KINESIS_LIST_STREAMS_MAX_ATTEMPTS = 1000;
    private static final int KINESIS_LIST_STREAMS_LIMIT = 30;

    private final KinesisClientBuilder kinesisClientBuilder;

    @Inject
    public KinesisService(KinesisClientBuilder kinesisClientBuilder) {

        this.kinesisClientBuilder = kinesisClientBuilder;
    }

    public KinesisClient createKinesisClient(String regionName, String accessKeyId, String secretAccessKey) {

        return kinesisClientBuilder.region(Region.of(regionName))
                .credentialsProvider(AWSService.validateCredentials(accessKeyId, secretAccessKey))
                .build();
    }

    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest heathCheckRequest) {

        // TODO: Read a log message from Kinesis.

        // Detect the log message format

        // TODO: Replace with actual log message received from Kinesis stream.
        String message = "2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 ACCEPT OK";
        AWSLogMessage awsLogMessage = new AWSLogMessage(message);

        return KinesisHealthCheckResponse.create(true,
                                                 awsLogMessage.messageType().toString(),
                                                 "Success! The message is an AWS FlowLog!");
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

        final KinesisClient kinesisClient = createKinesisClient(regionName, accessKeyId, secretAccessKey);

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


    public static void retrieveKinesisLogs(String kinesisStream, KinesisClient kinesisClient) {

        // TODO add error logging
        // Create ListShard request and response and designate the Kinesis stream
        ListShardsRequest listShardsRequest = ListShardsRequest.builder().streamName(kinesisStream).build();
        ListShardsResponse listShardsResponse = kinesisClient.listShards(listShardsRequest);

        String shardID;
        int shardNum = listShardsResponse.shards().size();
        // TODO delete this counter later
        int recordsInKinesisStream = 0;

        // Iterate through the shards that exist
        String nextShardIterator;
        for (int i = 0; i < shardNum; i++) {
            shardID = listShardsResponse.shards().get(i).shardId();
            GetShardIteratorRequest getShardIteratorRequest = GetShardIteratorRequest.builder()
                    .shardId(shardID)
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
                    System.out.println("[" + recordsInKinesisStream + "] " + new String(getRecordsResponse.records().get(k).data().asByteArray()));
                    recordsInKinesisStream++;
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
    }
}