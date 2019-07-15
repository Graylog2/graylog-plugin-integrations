package org.graylog.integrations.aws.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.inputs.AWSInput;
import org.graylog.integrations.aws.resources.requests.AWSInputCreateRequest;
import org.graylog.integrations.aws.resources.responses.AWSRegion;
import org.graylog.integrations.aws.resources.responses.AvailableService;
import org.graylog.integrations.aws.resources.responses.AvailableServiceResponse;
import org.graylog.integrations.aws.resources.responses.RegionsResponse;
import org.graylog.integrations.aws.transports.KinesisTransport;
import org.graylog2.database.NotFoundException;
import org.graylog2.inputs.Input;
import org.graylog2.inputs.InputService;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.users.User;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.transports.ThrottleableTransport;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.rest.models.system.inputs.requests.InputCreateRequest;
import org.graylog2.shared.inputs.MessageInputFactory;
import org.graylog2.shared.inputs.NoSuchInputTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.RegionMetadata;

import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for all AWS CloudWatch business logic and SDK usages.
 */
public class AWSService {

    private static final Logger LOG = LoggerFactory.getLogger(AWSService.class);

    private final InputService inputService;
    private final MessageInputFactory messageInputFactory;
    private final NodeId nodeId;

    @Inject
    public AWSService(InputService inputService, MessageInputFactory messageInputFactory, NodeId nodeId) {

        this.inputService = inputService;
        this.messageInputFactory = messageInputFactory;
        this.nodeId = nodeId;
    }

    /**
     * @return A list of all available regions.
     */
    public RegionsResponse getAvailableRegions() {

        List<AWSRegion> regions =
                Region.regions().stream()
                      // Ignore the global region. CloudWatch and Kinesis cannot be used with global regions.
                      .filter(r -> !r.isGlobalRegion())
                      .map(r -> {
                          // Build a single AWSRegionResponse with id, description, and displayValue.
                          RegionMetadata regionMetadata = r.metadata();
                          String label = String.format("%s: %s", regionMetadata.description(), regionMetadata.id());
                          return AWSRegion.create(regionMetadata.id(), label);
                      })
                      .sorted(Comparator.comparing(AWSRegion::regionId))
                      .collect(Collectors.toList());

        return RegionsResponse.create(regions, regions.size());
    }

    /**
     * Build a list of region choices with both a value (persisted in configuration) and display value (shown to the user).
     *
     * The display value is formatted nicely: "EU (London): eu-west-2"
     * The value is eventually passed to Regions.of() to get the actual region object: eu-west-2
     *
     * @return a choices map with configuration value map keys and display value map values.
     */
    public static Map<String, String> buildRegionChoices() {
        Map<String, String> regions = Maps.newHashMap();
        for (Region region : Region.regions()) {

            // Ignore the global region. CloudWatch and Kinesis cannot be used with global regions.
            if (region.isGlobalRegion()) {
                continue;
            }

            RegionMetadata regionMetadata = RegionMetadata.of(region);
            String displayValue = String.format("%s: %s", regionMetadata.description(), region.id());
            regions.put(region.id(), displayValue);
        }
        return regions;
    }

    /**
     * Checks that the supplied accessKey and secretKey are not null or blank
     *
     * @return A credential provider
     */
    static StaticCredentialsProvider buildCredentialProvider(String accessKeyId, String secretAccessKey) {
        Preconditions.checkArgument(StringUtils.isNotBlank(accessKeyId), "An AWS access key is required.");
        Preconditions.checkArgument(StringUtils.isNotBlank(secretAccessKey), "An AWS secret key is required.");

        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
    }

    /**
     * @return A list of available AWS services supported by the AWS Graylog AWS integration.
     */
    public AvailableServiceResponse getAvailableServices() {

        ArrayList<AvailableService> services = new ArrayList<>();
        AvailableService cloudWatchService =
                AvailableService.create("CloudWatch",
                                        "Retrieve CloudWatch logs via Kinesis. Kinesis allows streaming of the logs " +
                                        "in real time. AWS CloudWatch is a monitoring and management service built " +
                                        "for developers, system operators, site reliability engineers (SRE), " +
                                        "and IT managers.",
                                        "{\n" +
                                        "  \"Version\": \"2019-06-19\",\n" +
                                        "  \"Statement\": [\n" +
                                        "    {\n" +
                                        "      \"Sid\": \"GraylogCloudWatchPolicy\",\n" +
                                        "      \"Effect\": \"Allow\",\n" +
                                        "      \"Action\": [\n" +
                                        "        \"cloudwatch:PutMetricData\",\n" +
                                        "        \"dynamodb:CreateTable\",\n" +
                                        "        \"dynamodb:DescribeTable\",\n" +
                                        "        \"dynamodb:GetItem\",\n" +
                                        "        \"dynamodb:PutItem\",\n" +
                                        "        \"dynamodb:Scan\",\n" +
                                        "        \"dynamodb:UpdateItem\",\n" +
                                        "        \"ec2:DescribeInstances\",\n" +
                                        "        \"ec2:DescribeNetworkInterfaceAttribute\",\n" +
                                        "        \"ec2:DescribeNetworkInterfaces\",\n" +
                                        "        \"elasticloadbalancing:DescribeLoadBalancerAttributes\",\n" +
                                        "        \"elasticloadbalancing:DescribeLoadBalancers\",\n" +
                                        "        \"kinesis:GetRecords\",\n" +
                                        "        \"kinesis:GetShardIterator\",\n" +
                                        "        \"kinesis:ListShards\"\n" +
                                        "      ],\n" +
                                        "      \"Resource\": \"*\"\n" +
                                        "    }\n" +
                                        "  ]\n" +
                                        "}",
                                        "Requires Kinesis",
                                        "https://aws.amazon.com/cloudwatch/"
                );
        services.add(cloudWatchService);
        return AvailableServiceResponse.create(services, services.size());
    }

    /**
     * Save the AWS Input
     *
     * This method takes the individual input params in the {@link AWSInputCreateRequest} and creates/saves
     * an input with them.
     */
    public Input saveInput(AWSInputCreateRequest request, User user) throws Exception {

        // Transpose the SaveAWSInputRequest to the needed InputCreateRequest
        final HashMap<String, Object> configuration = new HashMap<>();
        configuration.put(AWSInput.CK_AWS_MESSAGE_TYPE, request.awsMessageType());
        configuration.put(AWSInput.CK_TITLE, request.name()); // TODO: Should name and title be the same?
        configuration.put(AWSInput.CK_DESCRIPTION, request.description());
        configuration.put(AWSInput.CK_GLOBAL, request.global());
        configuration.put(ThrottleableTransport.CK_THROTTLING_ALLOWED, request.throttlingAllowed());
        configuration.put(AWSInput.CK_AWS_REGION, request.region());
        configuration.put(AWSInput.CK_ACCESS_KEY, request.awsAccessKey());
        configuration.put(AWSInput.CK_SECRET_KEY, request.awsSecretKey());
        configuration.put(AWSInput.CK_ASSUME_ROLE_ARN, request.assumeRoleARN());

        AWSMessageType inputType = AWSMessageType.valueOf(request.awsMessageType());
        if (inputType.isKinesis()) {
            configuration.put(KinesisTransport.CK_KINESIS_STREAM_NAME, request.streamName());
            configuration.put(KinesisTransport.CK_KINESIS_RECORD_BATCH_SIZE, request.batchSize());
            configuration.put(KinesisTransport.CK_KINESIS_MAX_THROTTLED_WAIT_MS, request.kinesisMaxThrottledWaitMs());
        } else {
            throw new Exception("The specified input type is not supported.");
        }

        // Create and save the input.
        final InputCreateRequest inputCreateRequest = InputCreateRequest.create(request.name(),
                                                                                AWSInput.TYPE,
                                                                                false,
                                                                                configuration,
                                                                                nodeId.toString());
        try {
            final MessageInput messageInput = messageInputFactory.create(inputCreateRequest, user.getName(), nodeId.toString());
            messageInput.checkConfiguration();
            final Input input = this.inputService.create(messageInput.asMap());
            final String newInputId = inputService.save(input);
            LOG.debug("New AWS input created. id [{}] request [{}]", newInputId, request);
            return input;
        } catch (NoSuchInputTypeException e) {
            LOG.error("There is no such input type registered.", e);
            throw new NotFoundException("There is no such input type registered.", e);
        } catch (ConfigurationException e) {
            LOG.error("Missing or invalid input configuration.", e);
            throw new BadRequestException("Missing or invalid input configuration.", e);
        }
    }
}