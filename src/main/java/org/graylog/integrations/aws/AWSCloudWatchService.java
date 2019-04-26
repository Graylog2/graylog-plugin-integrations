package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.AWSCloudWatchResponse;
import org.graylog.integrations.aws.resources.AWSHeathCheckRequest;

/**
 * Service for all AWS CloudWatch business logic.
 */
public interface AWSCloudWatchService {

    AWSCloudWatchResponse healthCheck(AWSHeathCheckRequest request) throws AWSException;
}
