package org.graylog.integrations.okta;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.rest.resources.system.inputs.AbstractInputsResource;
import org.graylog2.shared.inputs.MessageInputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Api(value = "Okta", description = "Okta Integrations")
@Path("/okta")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OktaResource extends AbstractInputsResource implements PluginRestResource {

    public OktaService oktaService;

    OkHttpClient client = new OkHttpClient().newBuilder().build();
    // TODO delete logger
    private static final Logger LOG = LoggerFactory.getLogger(OktaCodec.class);

    @Inject
    public OktaResource(OktaService oktaService, MessageInputFactory messageInputFactory) throws IOException {
        super(messageInputFactory.getAvailableInputs());
        this.oktaService = oktaService;
    }

    @GET
    @Timed
    @Path("/logs")
    @ApiOperation(value = "Pull Okta System Logs", response = okhttp3.Response.class)
    public OktaResponse syslogs() throws Exception {
        // TODO add apiparam for hardcoded values
        String url = "https://company.okta.com/api/v1/logs";
        String apiKey = "SSWS ";
        LOG.info(url);
        LOG.info(apiKey);

        // TODO move method into OktaService.getSystemLogs
        OkHttpClient client = new OkHttpClient().newBuilder()
                                                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", apiKey)
                .build();

        okhttp3.Response response = client.newCall(request).execute();
        OktaResponse oktaResponse = OktaResponse.create(response.body().string());
        return oktaResponse;
    }
}