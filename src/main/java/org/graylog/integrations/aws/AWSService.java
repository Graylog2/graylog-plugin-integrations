package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.RegionMetadata;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for all AWS CloudWatch business logic.
 */
public class AWSService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);

    public KinesisHealthCheckResponse healthCheck(KinesisHealthCheckRequest heathCheckRequest) {

        // TODO: Read a log message from Kinesis.

        // Detect the log message format

        // TODO: Replace with actual log message received from Kinesis stream.
        String message = "2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 ACCEPT OK";
        AWSLogMessage awsLogMessage = new AWSLogMessage(message);

        return KinesisHealthCheckResponse.create(true,
                                                 awsLogMessage.messageType().toString(),
                                                 "Success! The message is an AWS FlowLog!");
    }

    /**
     * @return A list of all available regions.
     */
    public List<RegionResponse> getAvailableRegions() {

        return Region.regions().stream()
                     .filter(r -> !r.isGlobalRegion()) // Ignore the global region. We don't need it.
                     .map(r -> {
                         // Build a single AWSRegionResponse with id, description, and displayValue.
                         RegionMetadata regionMetadata = r.metadata();
                         String displayValue = String.format("%s: %s", regionMetadata.description(), regionMetadata.id());
                         return RegionResponse.create(regionMetadata.id(), regionMetadata.description(), displayValue);
                     }).collect(Collectors.toList());
    }
}