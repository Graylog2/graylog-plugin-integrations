package org.graylog.integrations.aws;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import org.apache.commons.lang3.StringUtils;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
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

/**
 * Service for all AWS CloudWatch business logic.
 */
public class AWSKinesisClient {

    private static final Logger LOG = LoggerFactory.getLogger(AWSKinesisClient.class);
    private static final int KINESIS_LIST_STREAMS_MAX_ATTEMPTS = 100;
    private static final int KINESIS_LIST_STREAMS_LIMIT = 30;

    final KinesisClientBuilder kinesisClientBuilder;

    @Inject
    public AWSKinesisClient(KinesisClientBuilder kinesisClientBuilder) {

        this.kinesisClientBuilder = kinesisClientBuilder;
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

        // TODO: Change to debug.
        LOG.info("Kinesis streams queried [{}]", streamNames);

        return streamNames;
    }

    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest request) {

        LOG.info("Attempting to perform AWS Kinesis health check [{}]", request);

        // TODO: Attempt to pull a few logs from Kinesis.

        // TODO: Run regex to identify log type.

        // TODO: How do we pass the detected log type into the setup save method later?

        return KinesisHealthCheckResponse.create(true, "CloudWatch", "Success");
    }
}