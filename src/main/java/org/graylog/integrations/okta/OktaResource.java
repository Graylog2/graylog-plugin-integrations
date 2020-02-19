package org.graylog.integrations.okta;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.rest.resources.system.inputs.AbstractInputsResource;
import org.graylog2.shared.inputs.MessageInputFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(value = "Okta", description = "Okta Integrations")
@Path("/Okta")
@RequiresAuthentication
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OktaResource extends AbstractInputsResource implements PluginRestResource {

    @Inject
    public OktaResource(MessageInputFactory messageInputFactory) {
        super(messageInputFactory.getAvailableInputs());
    }

    @GET
    @Timed
    @Path("/inputs")
    @ApiOperation(value = "Create a new Okta input.")
    public OktaResponse create(@ApiParam(name = "JSON body", required = true)
                               @Valid @NotNull OktaRequest saveRequest) throws Exception {
        // TODO create input
        return null;
    }

    @POST
    @Timed
    @Path("/system_logs")
    @ApiOperation(value = "Pull Okta System Logs.")
    public OktaResponse getSystemLogs(@ApiParam(name = "JSON body", required = true)
                                      @Valid @NotNull OktaRequestImpl saveRequest) throws Exception {
        // TODO get system logs
        return null;
    }
}