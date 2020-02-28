/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.integrations.okta;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.rest.resources.system.inputs.AbstractInputsResource;
import org.graylog2.shared.inputs.MessageInputFactory;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    public OktaResponse syslogs(@ApiParam(name = "domain", value = "The domain name where the system logs are located.", required = true)
                                    @QueryParam("domain") @DefaultValue("company.okta.com") String domain,
                                @ApiParam(name = "apiKey", value = "The API token key", required = true)
                                    @QueryParam("apiKey") @DefaultValue("SSW apiKey") String apiKey) throws Exception {

        return oktaService.getSystemLogs(domain, apiKey);
    }
}