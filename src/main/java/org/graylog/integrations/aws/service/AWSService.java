package org.graylog.integrations.aws.service;

import org.graylog.integrations.aws.AWSClient;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.RegionMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Service for all AWS CloudWatch business logic.
 *
 * This layer should not directly use the AWS SDK. All SDK operations should be performed in AWSClient.
 */
public class AWSService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);

    AWSClient awsClient;

    public AWSService(AWSClient awsClient) {
        this.awsClient = awsClient;
    }

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

        // This stream operation is just a way to convert a list of regions to the RegionResponse object.
        return Region.regions().stream()
                     .filter(r -> !r.isGlobalRegion()) // Ignore the global region. We don't need it.
                     .map(r -> {
                         // Build a single AWSRegionResponse with id, description, and displayValue.
                         RegionMetadata regionMetadata = r.metadata();
                         String displayValue = String.format("%s: %s", regionMetadata.description(), regionMetadata.id());
                         return RegionResponse.create(regionMetadata.id(), regionMetadata.description(), displayValue);
                     }).collect(Collectors.toList());
    }

    /**
     * @return A list of all available log groups if the user is authorized.
     * @param regionName
     */
    public List<String> getLogGroups(String regionName, String accessKeyId, String secretAccessKey) {

        // TODO: Call AWS to get a list of log groups.
        // TODO: Move this to AWSClient

        // Return some sample data
        ArrayList<String> logGroupNames = new ArrayList<>();
        logGroupNames.add("flow-logs");
        logGroupNames.add("application-logs");

        return logGroupNames;
    }

    /**
     * No business logic is required here. Pass through request to AWSService.
     * @return
     */
    public List<String> getKinesisStreams(String regionName, String accessKeyId, String secretAccessKey) throws ExecutionException {

        return awsClient.getKinesisStreams(regionName, accessKeyId, secretAccessKey);
    }
}