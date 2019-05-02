package org.graylog.integrations.aws.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog.integrations.aws.AWSCloudWatchService;
import org.graylog.integrations.aws.AWSService;
import org.graylog.integrations.aws.resources.requests.AWSHeathCheckRequest;
import org.graylog.integrations.aws.resources.responses.AWSCloudWatchResponse;
import org.graylog.integrations.aws.resources.responses.AWSLogGroupsResponse;
import org.graylog.integrations.aws.resources.responses.AWSRegionResponse;
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
public class AWSCloudWatchResource implements PluginRestResource {

    private AWSCloudWatchService awsCloudWatchService;
    private AWSService awsService;

    @Inject
    public AWSCloudWatchResource(AWSService awsService,
                                 AWSCloudWatchService awsCloudWatchService) {
        this.awsService = awsService;
        this.awsCloudWatchService = awsCloudWatchService;
    }

    @GET
    @Timed
    @Path("/regions")
    @ApiOperation(value = "Get all available AWS regions")
    public List<AWSRegionResponse> regions() {

        return awsService.getAvailableRegions();
    }

    // TODO: Pass in credentials some how.
    @GET
    @Timed
    @Path("/logGroups/{regionName}")
    @ApiOperation(value = "Get all available AWS log groups for the specified region")
    public AWSLogGroupsResponse logGroups(@ApiParam(name = "regionName", required = true)
                                              @PathParam("regionName") String regionName) {

        return awsCloudWatchService.getLogGroups(regionName);
    }

    @PUT
    @Timed
    @Path("/healthCheck")
    @ApiOperation(
            value = "Attempt to retrieve logs from the indicated AWS log group with the specified credentials.",
            response = AWSCloudWatchResponse.class
    )
    public Response update(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AWSHeathCheckRequest heathCheckRequest) {

        // TODO: Check permissions?

        // Call into service layer to handle business logic.
        AWSCloudWatchResponse response = awsCloudWatchService.healthCheck(heathCheckRequest);

        return Response.accepted().entity(response).build();
    }
}