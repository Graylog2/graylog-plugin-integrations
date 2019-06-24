package org.graylog.integrations.aws.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog.integrations.aws.resources.requests.KinesisInputCreateRequest;
import org.graylog.integrations.aws.service.KinesisService;
import org.graylog.integrations.aws.service.CloudWatchService;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.AvailableAWSServiceSummmary;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.graylog.integrations.aws.service.AWSService;
import org.graylog2.audit.AuditEventTypes;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;
import org.graylog2.shared.security.RestPermissions;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Web endpoints for the AWS integration.
 */
@RequiresAuthentication
@Api(value = "AWS", description = "AWS integrations")
@Path("/aws")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AWSResource extends RestResource implements PluginRestResource {

    private AWSService awsService;
    private KinesisService kinesisService;
    private CloudWatchService cloudWatchService;

    @Inject
    public AWSResource(AWSService awsService, KinesisService kinesisService, CloudWatchService cloudWatchService) {
        this.awsService = awsService;
        this.kinesisService = kinesisService;
        this.cloudWatchService = cloudWatchService;
    }

    @GET
    @Timed
    @Path("/regions")
    @ApiOperation(value = "Get all available AWS regions")
    public List<RegionResponse> getAwsRegions() {
        return awsService.getAvailableRegions();
    }

    @GET
    @Timed
    @Path("/availableServices")
    @ApiOperation(value = "Get all available AWS services")
    public AvailableAWSServiceSummmary getAvailableServices() {
        return awsService.getAvailableServices();
    }

    // TODO: Rework to accept a form post body with credentials
    @GET
    @Timed
    @Path("/cloudWatch/logGroups/{regionName}")
    @ApiOperation(value = "Get all available AWS CloudWatch log groups names for the specified region")
    public List<String> getLogGroupNames(@ApiParam(name = "regionName", required = true)
                                         @PathParam("regionName") String regionName) {
        return cloudWatchService.getLogGroupNames(regionName);
    }

    // TODO: Rework to accept a form post body with credentials
    @GET
    @Timed
    @Path("/kinesis/streams/{regionName}")
    @ApiOperation(value = "Get all available AWS Kinesis streams for the specified region")
    public List<String> getKinesisStreams(@ApiParam(name = "regionName", required = true)
                                          @PathParam("regionName") String regionName) throws ExecutionException {
        return kinesisService.getKinesisStreams(regionName, null, null);
    }

    /**
     * Performs an AWS HealthCheck
     *
     * Sample CURL command for executing this method. Use this to model the UI request.
     * Note the --data-binary param that includes the put body JSON with region and AWS credentials.
     *
     * curl 'http://someuser:somepass@localhost:9000/api/plugins/org.graylog.integrations/aws/kinesis/healthCheck' \
     * -X PUT \
     * -H 'X-Requested-By: XMLHttpRequest' \
     * -H 'Content-Type: application/json'   \
     * -H 'Accept: application/json' \
     * --data-binary '{
     *   "region": "us-east-1",
     *   "aws_access_key_id": "some-key",
     *   "aws_secret_access_key": "some-secret",
     *   "stream_name": "a-stream",
     *   "log_group_name": "a-log-group"
     * }'
     *
     */
    @PUT
    @Timed
    @Path("/kinesis/healthCheck")
    @ApiOperation(
            value = "Attempt to retrieve logs from the indicated AWS log group with the specified credentials.",
            response = KinesisHealthCheckResponse.class
    )
    public Response kinesisHealthCheck(@ApiParam(name = "JSON body", required = true) @Valid @NotNull KinesisHealthCheckRequest heathCheckRequest) throws ExecutionException, IOException {
        KinesisHealthCheckResponse response = kinesisService.healthCheck(heathCheckRequest);
        return Response.accepted().entity(response).build();
    }

    /**
     * Save the new AWS CloudWatch integration.
     *
     *  curl 'http://admin:123123123@localhost:9000/api/plugins/org.graylog.integrations/aws/kinesis/save' \
     *  -v \
     *  -X POST \
     *  -H 'X-Requested-By: just-a-test' \
     *  -H 'Content-Type: application/json' \
     *  -H 'Accept: application/json' \
     *  --compressed \
     *  --data-binary '{
     *      "aws_access_key": "",
     *      "aws_secret_key": "",
     *      "region": "us-east-1",
     *      "name": "New Flow Logs",
     *      "description": "Some flow logs.",
     *      "aws_input_type": "KINESIS_FLOW_LOGS",
     *      "stream_name": "flow-logs",
     *      "batch_size": 10000,
     *      "assume_role_arn": "",
     *      "global": false,
     *      "enable_throttling": false
     * }'
     */
    @POST
    @Timed
    @Path("/kinesis/inputs")
    @ApiOperation( value = "Save a new AWS Kinesis input" )
    @RequiresPermissions(RestPermissions.INPUTS_CREATE)
    @AuditEvent(type = AuditEventTypes.MESSAGE_INPUT_CREATE)
    public Response create(@ApiParam(name = "JSON body", required = true)
                           @Valid @NotNull KinesisInputCreateRequest saveRequest) throws Exception {

        awsService.saveInput(saveRequest, getCurrentUser());

        // TODO: Identify if this method needs to return a specific response with the id of the new input.
        return Response.ok().build();
    }
}