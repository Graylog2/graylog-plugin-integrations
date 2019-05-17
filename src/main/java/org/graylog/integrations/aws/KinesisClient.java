package org.graylog.integrations.aws;

import org.apache.commons.lang3.StringUtils;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.KinesisStreamsResponse;
import org.graylog2.shared.utilities.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.acm.model.LimitExceededException;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.KinesisException;
import software.amazon.awssdk.services.kinesis.model.ListStreamsRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;

import java.util.Locale;

/**
 * Service for all AWS CloudWatch business logic.
 */
public class KinesisClient {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisClient.class);

    /**
     * @param regionName The AWS region.
     * @return A list of all available Kinesis streams in the supplied region.
     */
    public KinesisStreamsResponse getKinesisStreams(String regionName, String accessKeyId, String secretAccessKey) {

        // Only explicitly provide credentials if key/secret are provided.
        final KinesisClientBuilder clientBuilder = software.amazon.awssdk.services.kinesis.KinesisClient.builder();
        if (StringUtils.isNotBlank(accessKeyId) && StringUtils.isNotBlank(secretAccessKey)) {
            StaticCredentialsProvider credentialsProvider =
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
            clientBuilder.credentialsProvider(credentialsProvider);
        }

        final software.amazon.awssdk.services.kinesis.KinesisClient kinesisClient = clientBuilder.build();
        final ListStreamsResponse response;
        try {
            response = kinesisClient.listStreams(ListStreamsRequest.builder().build());
        } catch (LimitExceededException e) {
            // Provide specific feedback that a limit has been exceeded.
            final String message = String.format(Locale.ENGLISH, "AWS Limit Exceeded: [%s] " +
                                                           "See https://docs.aws.amazon.com/streams/latest/dev/service-sizes-and-limits.html for more info.",
                                           ExceptionUtils.formatMessageCause(e));
            LOG.error(message, e);
            return KinesisStreamsResponse.create(null, false, message);
        } catch (KinesisException | SdkClientException e) {
            final String message = ExceptionUtils.formatMessageCause(e);
            LOG.error(message, e);
            return KinesisStreamsResponse.create(null, false, message);
        }

        return KinesisStreamsResponse.create(response.streamNames(), true, "Request was successful!");
    }

    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest request) {

        LOG.info("Attempting to perform AWS Kinesis health check [{}]", request);

        // TODO: Attempt to pull a few logs from Kinesis.

        // TODO: Run regex to identify log type.

        // TODO: How do we pass the detected log type into the setup save method later?

        return KinesisHealthCheckResponse.create(true, "CloudWatch", "Success");
    }
}