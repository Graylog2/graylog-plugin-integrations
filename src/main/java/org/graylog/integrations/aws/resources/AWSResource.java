package org.graylog.integrations.aws.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog.integrations.aws.AWSService;
import org.graylog.integrations.aws.CloudWatchService;
import org.graylog.integrations.aws.KinesisService;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.KinesisStreamsResponse;
import org.graylog.integrations.aws.resources.responses.LogGroupsResponse;
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

/**
 * Web endpoints for the AWS CloudWatch.
 *
 * Methods needed:
 *
 * 1) Authentication checker. Attempts to obtain a few CloudWatch logs. Returns any configuration
 * or authentication errors.
 *
 * 2) Log format detection response. May be able to combine this with the authentication checker. Should indicate what
 * type of log messages were detected.
 *
 * 3) Save configuration: Saves an AWS configuration.
 */

@RequiresAuthentication
@Api(value = "System/AWS Cloud Watch", description = "AWS CloudWatch integrations")
@Path("/system/aws")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AWSResource implements PluginRestResource {

    private CloudWatchService cloudWatchService;
    private KinesisService kinesisService;
    private AWSService awsService;

    @Inject
    public AWSResource(AWSService awsService,
                       CloudWatchService cloudWatchService, KinesisService kinesisService) {
        this.awsService = awsService;
        this.cloudWatchService = cloudWatchService;
        this.kinesisService = kinesisService;
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
    public LogGroupsResponse logGroups(@ApiParam(name = "regionName", required = true)
                                       @PathParam("regionName") String regionName) {

        return cloudWatchService.getLogGroups(regionName);
    }

    @GET
    @Timed
    @Path("/kinesisStreams/{regionName}")
    @ApiOperation(value = "Get all available AWS Kinesis streams for the specified region")
    public Response kinesisStreams(@ApiParam(name = "regionName", required = true)
                                   @PathParam("regionName") String regionName) {

        // TODO: Pass in credentials somehow.
        final KinesisStreamsResponse response = kinesisService.getKinesisStreams(regionName, null, null);

        // Return appropriate error HTTP status
        if (!response.success()) {
            return Response.serverError().entity(response).build();
        }

        // Else return success.
        return Response.ok().entity(response).build();
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
        KinesisHealthCheckResponse response = kinesisService.healthCheck(heathCheckRequest);

        return Response.accepted().entity(response).build();
    }
}