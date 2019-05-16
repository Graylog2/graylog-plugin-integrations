package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.responses.AWSKinesisStreamsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Service for all AWS CloudWatch business logic.
 */
public class AWSKinesisService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSKinesisService.class);

    /**
     * @return A list of all available Kinesis streams in the supplied region.
     * @param regionName
     */
    public AWSKinesisStreamsResponse getStreams(String regionName) {

        // TODO: Call AWS to get a list of streams.

        // Return some sample data
        ArrayList<String> streamNames = new ArrayList<>();
        streamNames.add("flow-logs");
        streamNames.add("application-logs");

        return AWSKinesisStreamsResponse.create(streamNames, true, "Request was successful!");
    }
}