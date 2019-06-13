package org.graylog.integrations.aws.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog.integrations.aws.KinesisService;
import org.graylog.integrations.aws.cloudwatch.CloudWatchService;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
import org.graylog.integrations.aws.service.AWSService;
import org.graylog2.plugin.rest.PluginRestResource;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
public class AWSResource implements PluginRestResource {

    private AWSService awsService;
    private KinesisService kinesisService;
    private CloudWatchService cloudWatchService;

    @Inject
    public AWSResource(AWSService awsService, KinesisService kinesisService, CloudWatchService cloudWatchService) {
        this.awsService = awsService;
        this.kinesisService = kinesisService;
        this.cloudWatchService = cloudWatchService;
    }

    // GET AWS regions
    @GET
    @Timed
    @Path("/regions")
    @ApiOperation(value = "Get all available AWS regions")
    public List<RegionResponse> getAwsRegions() {
        return awsService.getAvailableRegions();
    }

    // GET CloudWatch log group names
    @GET
    @Timed
    @Path("/cloudWatch/logGroups/{regionName}")
    @ApiOperation(value = "Get all available AWS CloudWatch log groups names for the specified region")
    public List<String> getLogGroupNames(@ApiParam(name = "regionName", required = true)
                                         @PathParam("regionName") String regionName) {

        return cloudWatchService.getLogGroupNames(regionName);
    }

    // GET Kinesis Streams
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

        // TODO: Check permissions?

        // Call into service layer to handle business logic.
        KinesisHealthCheckResponse response = kinesisService.healthCheck(heathCheckRequest);

        return Response.accepted().entity(response).build();
    }

    // TODO  GET kinesisAutomatedSetup
    // getRegion, getlogGroupNames, subscribeToStream
}