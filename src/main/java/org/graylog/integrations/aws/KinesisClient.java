package org.graylog.integrations.aws;

import org.apache.commons.lang3.StringUtils;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
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

import java.util.List;
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
    public List<String> getKinesisStreams(String regionName, String accessKeyId, String secretAccessKey) {

        // Only explicitly provide credentials if key/secret are provided.
        final KinesisClientBuilder clientBuilder = software.amazon.awssdk.services.kinesis.KinesisClient.builder();
        if (StringUtils.isNotBlank(accessKeyId) && StringUtils.isNotBlank(secretAccessKey)) {
            StaticCredentialsProvider credentialsProvider =
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
            clientBuilder.credentialsProvider(credentialsProvider);
        }

        final software.amazon.awssdk.services.kinesis.KinesisClient kinesisClient = clientBuilder.build();
        return kinesisClient.listStreams(ListStreamsRequest.builder().build()).streamNames();
    }

    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest request) {

        LOG.info("Attempting to perform AWS Kinesis health check [{}]", request);

        // TODO: Attempt to pull a few logs from Kinesis.

        // TODO: Run regex to identify log type.

        // TODO: How do we pass the detected log type into the setup save method later?

        return KinesisHealthCheckResponse.create(true, "CloudWatch", "Success");
    }
}