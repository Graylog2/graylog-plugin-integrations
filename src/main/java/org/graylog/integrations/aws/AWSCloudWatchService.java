package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.AWSHeathCheckRequest;
import org.graylog.integrations.aws.resources.responses.AWSCloudWatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for all AWS CloudWatch business logic.
 */
public class AWSCloudWatchService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSCloudWatchService.class);

    public AWSCloudWatchResponse healthCheck(AWSHeathCheckRequest request) throws AWSException {

        LOG.info("Attempting to perform AWS CloudWatch health check [{}]", request);

        // TODO: Attempt to pull some CloudWatch logs here.

        // TODO: Run regex to identify log type.

        return AWSCloudWatchResponse.create(true, "CloudWatch", "Success");
    }
}