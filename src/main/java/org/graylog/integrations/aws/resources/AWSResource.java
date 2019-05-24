package org.graylog.integrations.aws.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog.integrations.aws.AWSClient;
import org.graylog.integrations.aws.service.AWSService;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.RegionResponse;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Web endpoints for the AWS integration.
 */

@RequiresAuthentication
@Api(value = "System/AWS", description = "AWS integrations")
@Path("/system/aws")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AWSResource implements PluginRestResource {

    private AWSService awsService;

    @Inject
    public AWSResource(AWSService awsService, AWSClient kinesisClient) {
        this.awsService = awsService;
    }

    @GET
    @Timed
    @Path("/regions")
    @ApiOperation(value = "Get all available AWS regions")
    public List<RegionResponse> regions() {

        return awsService.getAvailableRegions();
    }

    // TODO: Pass in credentials somehow.
    @GET
    @Timed
    @Path("/logGroups/{regionName}")
    @ApiOperation(value = "Get all available AWS log groups for the specified region")
    public List<String> logGroups(@ApiParam(name = "regionName", required = true)
                                       @PathParam("regionName") String regionName) {

        // TODO: Implement the contents of the cloudWatchService.getLogGroups method.
        return awsService.getLogGroups(regionName, null, null);
    }

    @GET
    @Timed
    @Path("/kinesisStreams/{regionName}")
    @ApiOperation(value = "Get all available AWS Kinesis streams for the specified region")
    public List<String> kinesisStreams(@ApiParam(name = "regionName", required = true)
                                       @PathParam("regionName") String regionName) throws ExecutionException {

        return awsService.getKinesisStreams(regionName, null, null);
    }

    @PUT
    @Timed
    @Path("/kinesisHealthCheck")
    @ApiOperation(
            value = "Attempt to retrieve logs from the indicated AWS log group with the specified credentials.",
            response = KinesisHealthCheckResponse.class
    )
    public Response kinesisHealthCheck(@ApiParam(name = "JSON body", required = true) @Valid @NotNull KinesisHealthCheckRequest heathCheckRequest) {

        // TODO: Check permissions?

        // Call into service layer to handle business logic.
        KinesisHealthCheckResponse response = awsService.healthCheck(heathCheckRequest);

        return Response.accepted().entity(response).build();
    }
}