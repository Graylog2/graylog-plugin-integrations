package org.graylog.integrations.aws.resources;

import com.codahale.metrics.annotation.Timed;
import io.netty.handler.codec.http.FullHttpRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog.integrations.aws.AWSPermissions;
import org.graylog.integrations.aws.resources.requests.AWSRequestImpl;
import org.graylog.integrations.aws.resources.requests.CreateLogSubscriptionPolicyRequest;
import org.graylog.integrations.aws.resources.requests.CreateLogSubscriptionRequest;
import org.graylog.integrations.aws.resources.requests.KinesisFullSetupRequest;
import org.graylog.integrations.aws.resources.requests.KinesisNewStreamRequest;
import org.graylog.integrations.aws.resources.responses.CreateLogSubscriptionPolicyResponse;
import org.graylog.integrations.aws.resources.responses.CreateLogSubscriptionResponse;
import org.graylog.integrations.aws.resources.responses.KinesisFullSetupResponse;
import org.graylog.integrations.aws.resources.responses.KinesisFullSetupResponseStep;
import org.graylog.integrations.aws.resources.responses.KinesisNewStreamResponse;
import org.graylog.integrations.aws.resources.responses.LogGroupsResponse;
import org.graylog.integrations.aws.service.KinesisService;
import org.graylog2.plugin.rest.PluginRestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 * Web endpoints for the Kinesis auto-setup.
 */
@Api(value = "AWSKinesis", description = "AWS Kinesis auto-setup")
@Path("/kinesis")
@RequiresAuthentication
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KinesisSetupResource implements PluginRestResource {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisSetupResource.class);

    private KinesisService kinesisService;

    @Inject
    public KinesisSetupResource(KinesisService kinesisService) {
        this.kinesisService = kinesisService;
    }

    /**
     * 1.  Create a new Kinesis stream and check that its active
     * INPUT: credentials and streamName acquire from the user
     * OUTPUT: String streamArn
     * BACKEND DETAILS:
     * void createKinesisStream(KinesisClient kinesisClient, String stream)
     * StreamDescription streamDescription = checkKinesisStreamStatus(kinesisClient, stream);
     * String streamArn = streamDescription.streamARN();
     */
    @POST
    @Timed
    @Path("/kinesis/create_stream")
    @ApiOperation(value = "Attempt to create a new kinesis stream and wait for it to be ready.")
    @RequiresPermissions(AWSPermissions.AWS_READ)
    public KinesisNewStreamResponse createNewKinesisStream(@ApiParam(name = "JSON body", required = true) @Valid @NotNull
                                                                   KinesisNewStreamRequest request) {

        LOG.info("Request: [{}]", request);
        // Real method call is already implemented. Commented out for now to allow UI to be mocked out easier.
        // kinesisService.createNewKinesisStream(kinesisNewStreamRequest)

        // Mock response
        return KinesisNewStreamResponse.create(request.streamName(), "a-fake-arn", "The stream is good-to-go");
    }

    /**
     * 2.  Add role with required permissions and acquire roleArn
     * INPUT: roleName acquired from the user
     * OUTPUT: streamARN acquired from step 3
     * BACKEND DETAILS:
     * void setRolePermissions(iam, roleName, streamArn, region.toString());
     * String roleArn = getNewRolePermissions(iam, roleName);
     */
    @POST
    @Timed
    @Path("/create_subscription_policy")
    @ApiOperation(value = "Create AWS IAM policy needed for CloudWatch to write logs to Kinesis.")
    @RequiresPermissions(AWSPermissions.AWS_READ)
    public CreateLogSubscriptionPolicyResponse createPolicies(@ApiParam(name = "JSON body", required = true) @Valid @NotNull
                                                                      CreateLogSubscriptionPolicyRequest request) {
        LOG.info("Request: [{}]", request);

        // Mock response
        return CreateLogSubscriptionPolicyResponse.create("fake-policy-name", "fake-policy-arn");
    }

    /**
     * Creates a {@see <a href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/SubscriptionFilters.html">Cloud Watch Subscription Filter</a>},
     * which subscribes a kinesis stream to a CloudWatch log group. This will cause batches of CloudWatch loge messages
     * to be put in the Kinesis stream in a GZipped JSON byte array payload.
     *
     * 3.  Add the subscription to the Cloudwatch log group for kinesis
     * INPUT: credentials, logGroup, filterName, filterPattern acquired from user
     * OUTPUT: streamArn acquired from step 3
     * BACKEND DETAILS:
     * roleArn acquired from step 4
     * void addSubscriptionFilter(String logGroup, CloudWatchLogsClient cloudWatch, String streamArn, String roleArn, String filterName, String filterPattern)
     */
    @POST
    @Timed
    @Path("/add_subscription")
    @ApiOperation(value = "Subscribe a Kinesis stream to a CloudWatch log group. See https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/SubscriptionFilters.html")
    @RequiresPermissions(AWSPermissions.AWS_READ)
    public CreateLogSubscriptionResponse createSubscription(@ApiParam(name = "JSON body", required = true) @Valid @NotNull
                                                                    CreateLogSubscriptionRequest request) {
        LOG.info("Request: [{}]", request);

        // TODO: We'll need to give some thought to how to effectively build the UI for the filterPattern and filterName
        //   Perhaps we can provide default initialized values (eg. " " for filterPattern [matches all], and some generic pattern name).

        // Mock response
        return CreateLogSubscriptionResponse.create("Subscription created successfully");
    }

    /**
     * Full Kinesis setup.
     *
     * @param request
     * @return
     */
    @POST
    @Timed
    @Path("/full_setup")
    @ApiOperation(value = "Get all available AWS CloudWatch log groups names for the specified region.")
    @RequiresPermissions(AWSPermissions.AWS_READ)
    public KinesisFullSetupResponse addSubscription(@ApiParam(name = "JSON body", required = true) @Valid @NotNull
                                                            KinesisFullSetupRequest request) {

        LOG.info("Request: [{}]", request);

        // Mock response.
        final ArrayList<KinesisFullSetupResponseStep> setupSteps = new ArrayList<>();
        setupSteps.add(KinesisFullSetupResponseStep.create(true, "Create Stream", ""));
        setupSteps.add(KinesisFullSetupResponseStep.create(true, "Create Policy", ""));
        setupSteps.add(KinesisFullSetupResponseStep.create(false, "Subscribe stream to group", "Failed to create the subscription [Some specific AWS error]"));
        return KinesisFullSetupResponse.create(false, "Auto-setup was not fully successful!", setupSteps);
    }
}