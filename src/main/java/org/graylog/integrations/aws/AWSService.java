package org.graylog.integrations.aws;

import org.graylog.integrations.aws.resources.responses.AWSRegionResponse;
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

    /**
     * @return A list of all available regions.
     */
    public List<AWSRegionResponse> getAvailableRegions() {

        return Region.regions().stream()
                     .filter(r -> !r.isGlobalRegion()) // Ignore the global region. We don't need it.
                     .map(r -> {
                         // Build a single AWSRegionResponse with id, description, and displayValue.
                         RegionMetadata regionMetadata = r.metadata();
                         String displayValue = String.format("%s: %s", regionMetadata.description(), regionMetadata.id());
                         return AWSRegionResponse.create(regionMetadata.id(), regionMetadata.description(), displayValue);
                     }).collect(Collectors.toList());
    }
}