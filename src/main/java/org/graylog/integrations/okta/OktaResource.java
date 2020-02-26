package org.graylog.integrations.okta;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.rest.resources.system.inputs.AbstractInputsResource;
import org.graylog2.shared.inputs.MessageInputFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(value = "Okta", description = "Okta Integrations")
@Path("/okta")
@Produces(MediaType.APPLICATION_JSON)
public class OktaResource extends AbstractInputsResource implements PluginRestResource {

    public OktaService oktaService;

    @Inject
    public OktaResource(OktaService oktaService, MessageInputFactory messageInputFactory) {
        super(messageInputFactory.getAvailableInputs());
        this.oktaService = oktaService;
    }

    @GET
    @Timed
    @Path("/logs")
    @ApiOperation(value = "Pull Okta System Logs", response = OktaResponse.class)
    public OktaResponse syslogs() throws Exception {
        // TODO add apiparam for hardcoded values
        String domain = "https://company.okta.com/api/v1/logs";
        String apiKey = "SSWS ";

        return oktaService.getSystemLogs(domain, apiKey);
    }
}