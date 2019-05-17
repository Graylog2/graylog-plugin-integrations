package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.KinesisStreamsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Service for all AWS CloudWatch business logic.
 */
public class KinesisService {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisService.class);

    /**
     * @return A list of all available Kinesis streams in the supplied region.
     * @param regionName
     */
    public KinesisStreamsResponse getStreams(String regionName) {

        // TODO: Call AWS to get a list of streams.

        // Return some sample data
        ArrayList<String> streamNames = new ArrayList<>();
        streamNames.add("flow-logs");
        streamNames.add("application-logs");

        return KinesisStreamsResponse.create(streamNames, true, "Request was successful!");
    }

    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest request) {

        LOG.info("Attempting to perform AWS Kinesis health check [{}]", request);

        // TODO: Attempt to pull a few logs from Kinesis.

        // TODO: Run regex to identify log type.

        // TODO: How do we pass the detected log type into the setup save method later?

        return KinesisHealthCheckResponse.create(true, "CloudWatch", "Success");
    }
}