package org.graylog.integrations.aws.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog.integrations.aws.AWSPermissions;
import org.graylog.integrations.aws.resources.requests.AWSInputCreateRequest;
import org.graylog.integrations.aws.resources.requests.AWSRequestImpl;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.AvailableServiceResponse;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.LogGroupsResponse;
import org.graylog.integrations.aws.resources.responses.RegionsResponse;
import org.graylog.integrations.aws.resources.responses.StreamsResponse;
import org.graylog.integrations.aws.service.AWSService;
import org.graylog.integrations.aws.service.CloudWatchService;
import org.graylog.integrations.aws.service.KinesisService;
import org.graylog2.audit.AuditEventTypes;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.inputs.Input;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.rest.resources.system.inputs.AbstractInputsResource;
import org.graylog2.shared.inputs.MessageInputFactory;
import org.graylog2.shared.security.RestPermissions;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Web endpoints for the AWS integration.
 * Full base URL for requests in this class: http://api/plugins/org.graylog.integrations/aws/
 */
@Api(value = "AWS", description = "AWS integrations")
@Path("/aws")
@RequiresAuthentication
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AWSResource extends AbstractInputsResource implements PluginRestResource {

    private AWSService awsService;
    private KinesisService kinesisService;
    private CloudWatchService cloudWatchService;

    @Inject
    public AWSResource(AWSService awsService, KinesisService kinesisService, CloudWatchService cloudWatchService,
                       MessageInputFactory messageInputFactory) {
        super(messageInputFactory.getAvailableInputs());
        this.awsService = awsService;
        this.kinesisService = kinesisService;
        this.cloudWatchService = cloudWatchService;
    }

    @GET
    @Timed
    @Path("/regions")
    @ApiOperation(value = "Get all available AWS regions")
    @RequiresPermissions(AWSPermissions.AWS_READ)
    public RegionsResponse getAwsRegions() {
        return awsService.getAvailableRegions();
    }

    @GET
    @Timed
    @Path("/available_services")
    @ApiOperation(value = "Get all available AWS services")
    @RequiresPermissions(AWSPermissions.AWS_READ)
    public AvailableServiceResponse getAvailableServices() {
        return awsService.getAvailableServices();
    }

    /**
     * Get all available AWS CloudWatch log groups names for the specified region.
     */
    @POST
    @Timed
    @Path("/cloudwatch/log_groups")
    @ApiOperation(value = "Get all available AWS CloudWatch log groups names for the specified region.")
    @RequiresPermissions(AWSPermissions.AWS_READ)
    public LogGroupsResponse getLogGroupNames(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AWSRequestImpl awsRequest) {
        return cloudWatchService.getLogGroupNames(awsRequest.region(), awsRequest.awsAccessKeyId(), awsRequest.awsSecretAccessKey());
    }

    /**
     * Get all available Kinesis streams for the specified region.
     */
    @POST
    @Timed
    @Path("/kinesis/streams")
    @ApiOperation(value = "Get all available Kinesis streams for the specified region.")
    @RequiresPermissions(AWSPermissions.AWS_READ)
    public StreamsResponse getKinesisStreams(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AWSRequestImpl awsRequest) throws ExecutionException {
        return kinesisService.getKinesisStreamNames(awsRequest.region(), awsRequest.awsAccessKeyId(), awsRequest.awsSecretAccessKey());
    }

    /**
     * Perform an AWS HealthCheck
     */
    @POST
    @Timed
    @Path("/kinesis/health_check")
    @ApiOperation(
            value = "Attempt to retrieve logs from the indicated AWS log group with the specified credentials.",
            response = KinesisHealthCheckResponse.class
    )
    @RequiresPermissions(AWSPermissions.AWS_READ)
    public Response kinesisHealthCheck(@ApiParam(name = "JSON body", required = true) @Valid @NotNull KinesisHealthCheckRequest heathCheckRequest) throws ExecutionException, IOException {
        KinesisHealthCheckResponse response = kinesisService.healthCheck(heathCheckRequest);
        return Response.accepted().entity(response).build();
    }

    /**
     * Create a new AWS input.
     */
    @POST
    @Timed
    @Path("/inputs")
    @ApiOperation(value = "Create a new AWS input.")
    @RequiresPermissions(RestPermissions.INPUTS_CREATE)
    @AuditEvent(type = AuditEventTypes.MESSAGE_INPUT_CREATE)
    public Response create(@ApiParam(name = "JSON body", required = true)
                           @Valid @NotNull AWSInputCreateRequest saveRequest) throws Exception {

        Input input = awsService.saveInput(saveRequest, getCurrentUser());
        return Response.ok().entity(getInputSummary(input)).build();
    }
}