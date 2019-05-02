package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.requests.AWSHeathCheckRequest;
import org.graylog.integrations.aws.resources.responses.AWSCloudWatchResponse;
import org.graylog.integrations.aws.resources.responses.AWSLogGroupsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Service for all AWS CloudWatch business logic.
 */
public class AWSCloudWatchService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSCloudWatchService.class);

    public AWSCloudWatchResponse healthCheck(AWSHeathCheckRequest request) {

        LOG.info("Attempting to perform AWS CloudWatch health check [{}]", request);

        // TODO: Attempt to pull some CloudWatch logs here.

        // TODO: Run regex to identify log type.

        return AWSCloudWatchResponse.create(true, "CloudWatch", "Success");
    }

    /**
     * @return A list of all available regions if the user is authorized.
     * @param regionName
     */
    public AWSLogGroupsResponse getLogGroups(String regionName) {

        // TODO: Call AWS to get a list of log groups.

        // Return some sample data
        ArrayList<String> logGroupNames = new ArrayList<>();
        logGroupNames.add("flow-logs");
        logGroupNames.add("application-logs");

        return AWSLogGroupsResponse.create(logGroupNames, true, "Request was successful!");
    }
}