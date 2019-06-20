package org.graylog.integrations.aws.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.inputs.AWSInput;
import org.graylog.integrations.aws.resources.requests.KinesisInputCreateRequest;
import org.graylog.integrations.aws.resources.responses.AvailableAWSService;
import org.graylog.integrations.aws.resources.responses.AvailableAWSServiceSummmary;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.graylog.integrations.aws.transports.KinesisTransport;
import org.graylog2.database.NotFoundException;
import org.graylog2.inputs.Input;
import org.graylog2.inputs.InputService;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.users.User;
import org.graylog2.plugin.inputs.MessageInput;
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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for all AWS CloudWatch business logic.
 * <p>
 * This layer should not directly use the AWS SDK. All SDK operations should be performed in AWSClient.
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
    public List<RegionResponse> getAvailableRegions() {

        return Region.regions().stream()
                     // Ignore the global region. CloudWatch and Kinesis cannot be used with global regions.
                     .filter(r -> !r.isGlobalRegion())
                     .map(r -> {
                         // Build a single AWSRegionResponse with id, description, and displayValue.
                         RegionMetadata regionMetadata = r.metadata();
                         String displayValue = String.format("%s: %s", regionMetadata.description(), regionMetadata.id());
                         return RegionResponse.create(regionMetadata.id(), regionMetadata.description(), displayValue);
                     }).collect(Collectors.toList());
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
    public AvailableAWSServiceSummmary getAvailableServices() {

        ArrayList<AvailableAWSService> services = new ArrayList<>();
        AvailableAWSService cloudWatchService =
                AvailableAWSService.create("CloudWatch",
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
        return AvailableAWSServiceSummmary.create(services, services.size());
    }

    /**
     * Save the AWS Input
     */
    public void saveInput(KinesisInputCreateRequest request, User user) throws Exception {

        // Transpose the SaveAWSInputRequest to the needed InputCreateRequest
        // TODO: Correctly handle global field.
        // TODO: Do we save the description?
        final HashMap<String, Object> configuration = new HashMap<>();
        AWSMessageType inputType = AWSMessageType.valueOf(request.getAwsInputType());
        configuration.put(AWSInput.CK_AWS_INPUT_TYPE, inputType);
        configuration.put(AWSInput.CK_TITLE, request.name()); // TODO: Should name and title be the same?
        configuration.put(AWSInput.CK_DESCRIPTION, request.description());
        configuration.put(AWSInput.CK_GLOBAL, request.region());
        configuration.put(AWSInput.CK_AWS_REGION, request.region());
        configuration.put(AWSInput.CK_ACCESS_KEY, request.awsAccessKey());
        configuration.put(AWSInput.CK_SECRET_KEY, request.awsSecretKey());
        configuration.put(AWSInput.CK_ASSUME_ROLE_ARN, request.assumeRoleARN());

        if (inputType.isKinesis()) {
            configuration.put(KinesisTransport.CK_KINESIS_STREAM_NAME, request.streamName());
            configuration.put(KinesisTransport.CK_KINESIS_RECORD_BATCH_SIZE, request.batchSize());
            configuration.put(KinesisTransport.CK_KINESIS_MAX_THROTTLED_WAIT_MS, request.enableThrottling());
            // TODO: Put the remaining configuration values.
        } else {
            throw new Exception("The specified input type is not supported.");
        }

        final InputCreateRequest inputCreateRequest = InputCreateRequest.create(request.name(),
                                                                                AWSInput.TYPE,
                                                                                false,
                                                                                configuration,
                                                                                nodeId.toString());
        try {
            // Create the input object and save it to the database.
            final MessageInput messageInput = messageInputFactory.create(inputCreateRequest, user.getName(), nodeId.toString());
            messageInput.checkConfiguration();

            final Input input = this.inputService.create(messageInput.asMap());

            // TODO: What do we need this for? For saving to the DB?
            final String newInputId = inputService.save(input);

        } catch (NoSuchInputTypeException e) {
            LOG.error("There is no such input type registered.", e);
            throw new NotFoundException("There is no such input type registered.", e);
        } catch (ConfigurationException e) {
            LOG.error("Missing or invalid input configuration.", e);
            throw new BadRequestException("Missing or invalid input configuration.", e);
        }
    }
}