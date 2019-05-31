package org.graylog.integrations.aws;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import org.apache.commons.lang3.StringUtils;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.service.AWSLogMessage;
import org.graylog.integrations.aws.service.AWSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.ListStreamsRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class KinesisService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);
    private static final int KINESIS_LIST_STREAMS_MAX_ATTEMPTS = 100;
    private static final int KINESIS_LIST_STREAMS_LIMIT = 30;

    private final KinesisClientBuilder kinesisClientBuilder;

    @Inject
    public KinesisService(KinesisClientBuilder kinesisClientBuilder) {

        this.kinesisClientBuilder = kinesisClientBuilder;
    }

    static KinesisClient getKinesisClient() {
        KinesisClient kinesisClient = KinesisClient.create();
        return kinesisClient;
    }

    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest heathCheckRequest) {

        // TODO: Check if the Kinesis stream exists.
        // TODO: Replace with result for actual stream check.
        boolean streamExists = true;
        if (!streamExists) {
            return KinesisHealthCheckResponse.create(false,
                                                     AWSLogMessage.Type.UNKNOWN.toString(),
                                                     "The stream does not exist."); // TODO: Include specific error message here.
        }

        // TODO: Try to read log message from Kinesis.

        // Detect the log message format
        // TODO: Replace with actual log message received from Kinesis stream.
        String message = "2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 ACCEPT OK";
        AWSLogMessage awsLogMessage = new AWSLogMessage(message);

        // TODO: Add parsing logic
        // Add a codec that knows how to parse each message type.

        return KinesisHealthCheckResponse.create(true,
                                                 awsLogMessage.detectLogMessageType().toString(),
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

        // Only explicitly provide credentials if key/secret are provided.
        // TODO: Remove this IF check and always provided the string credentials. This will prevent the AWS SDK
        //  from reading credentials from environment variables, which we definitely do not want to do.
        if (StringUtils.isNotBlank(accessKeyId) && StringUtils.isNotBlank(secretAccessKey)) {
            StaticCredentialsProvider credentialsProvider =
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
