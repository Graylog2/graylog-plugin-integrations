package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.responses.LogGroupsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Service for all AWS CloudWatch business logic.
 */
public class CloudWatchService {

    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchService.class);

    /**
     * @return A list of all available log groups if the user is authorized.
     * @param regionName
     */
    public LogGroupsResponse getLogGroups(String regionName) {

        // TODO: Call AWS to get a list of log groups.

        // Return some sample data
        ArrayList<String> logGroupNames = new ArrayList<>();
        logGroupNames.add("flow-logs");
        logGroupNames.add("application-logs");

        return LogGroupsResponse.create(logGroupNames, true, "Request was successful!");
    }
}