package org.graylog.integrations.aws.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog.integrations.aws.AWSCloudWatchService;
import org.graylog.integrations.aws.AWSException;
import org.graylog2.audit.AuditEventTypes;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.shared.rest.resources.RestResource;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
public class AWSCloudWatchResource extends RestResource {

    private AWSCloudWatchService awsCloudWatchService;

    @Inject
    public AWSCloudWatchResource(AWSCloudWatchService awsCloudWatchService) {
        this.awsCloudWatchService = awsCloudWatchService;
    }

    @PUT
    @Timed
    @Path("/healthCheck")
    @ApiOperation(
            value = "Attempt to retrieve logs from the indicated AWS log group with the specified credentials.",
            response = AWSCloudWatchResponse.class
    )
    @ApiResponses(value = {
            // TODO: TBD
            // @ApiResponse(code = 400, message = "Some AWS error?")
    })
    @AuditEvent(type = AuditEventTypes.MESSAGE_INPUT_UPDATE)
    public Response update(@ApiParam(name = "JSON body", required = true) @Valid @NotNull
                                   AWSHeathCheckRequest heathCheckRequest) throws AWSException {

        // TODO: Check permissions?

        // Call into service layer to handle business logic.
        AWSCloudWatchResponse response = awsCloudWatchService.healthCheck(heathCheckRequest);

        return Response.accepted().entity(response).build();
    }
}