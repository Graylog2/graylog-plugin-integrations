package org.graylog.integrations.aws.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog.integrations.aws.resources.requests.AWSRequestImpl;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.AvailableServiceResponse;
import org.graylog.integrations.aws.resources.responses.HealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.LogGroupsResponse;
import org.graylog.integrations.aws.resources.responses.RegionsResponse;
import org.graylog.integrations.aws.resources.responses.StreamsResponse;
import org.graylog.integrations.aws.service.AWSService;
import org.graylog.integrations.aws.service.CloudWatchService;
import org.graylog.integrations.aws.service.KinesisService;
import org.graylog2.plugin.rest.PluginRestResource;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Web endpoints for the AWS integration.
 * Full base URL for requests in this class: http://api/plugins/org.graylog.integrations/aws/
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

    @GET
    @Timed
    @Path("/regions")
    @ApiOperation(value = "Get all available AWS regions")
    public List<RegionsResponse> getAwsRegions() {
        return awsService.getAvailableRegions();
    }

    @GET
    @Timed
    @Path("/available_services")
    @ApiOperation(value = "Get all available AWS services")
    public AvailableServiceResponse getAvailableServices() {
        return awsService.getAvailableServices();
    }

    /**
     * Get all available AWS CloudWatch log groups names for the specified region.
     *
     * Example request:
     * curl 'http://user:pass@localhost:9000/api/plugins/org.graylog.integrations/aws/cloudWatch/logGroups' \
     * -X POST \
     * -H 'X-Requested-By: XMLHttpRequest' \
     * -H 'Content-Type: application/json'   \
     * -H 'Accept: application/json' \
     * --data-binary '{
     * "region": "us-east-1",
     * "aws_access_key_id": "some-key",
     * "aws_secret_access_key": "some-secret"
     * }'
     */
    @POST
    @Timed
    @Path("/cloudwatch/log_groups")
    @ApiOperation(value = "Get all available AWS CloudWatch log groups names for the specified region.")
    public LogGroupsResponse getLogGroupNames(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AWSRequestImpl awsRequest) {
        return cloudWatchService.getLogGroupNames(awsRequest.region(), awsRequest.awsAccessKeyId(), awsRequest.awsSecretAccessKey());
    }

    /**
     * Get all available Kinesis streams for the specified region.
     *
     * Example request:
     * curl 'http://user:pass@localhost:9000/api/plugins/org.graylog.integrations/aws/kinesis/streams' \
     * -X POST \
     * -H 'X-Requested-By: XMLHttpRequest' \
     * -H 'Content-Type: application/json'   \
     * -H 'Accept: application/json' \
     * --data-binary '{
     * "region": "us-east-1",
     * "aws_access_key_id": "some-key",
     * "aws_secret_access_key": "some-secret"
     * }'
     */
    @POST
    @Timed
    @Path("/kinesis/streams")
    @ApiOperation(value = "Get all available Kinesis streams for the specified region.")
    public StreamsResponse getKinesisStreams(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AWSRequestImpl awsRequest) throws ExecutionException {
        return kinesisService.getKinesisStreamNames(awsRequest.region(), awsRequest.awsAccessKeyId(), awsRequest.awsSecretAccessKey());
    }

    /**
     * Performs an AWS HealthCheck
     *
     * Sample CURL command for executing this method. Use this to model the UI request.
     * Note the --data-binary param that includes the put body JSON with region and AWS credentials.
     *
     * curl 'http://user:pass@localhost:9000/api/plugins/org.graylog.integrations/aws/kinesis/healthCheck' \
     * -X POST \
     * -H 'X-Requested-By: XMLHttpRequest' \
     * -H 'Content-Type: application/json'   \
     * -H 'Accept: application/json' \
     * --data-binary '{
     * "region": "us-east-1",
     * "aws_access_key_id": "some-key",
     * "aws_secret_access_key": "some-secret",
     * "stream_name": "a-stream",
     * "log_group_name": "a-log-group"
     * }'
     */
    @POST
    @Timed
    @Path("/kinesis/health_check")
    @ApiOperation(
            value = "Attempt to retrieve logs from the indicated AWS log group with the specified credentials.",
            response = HealthCheckResponse.class
    )
    public Response kinesisHealthCheck(@ApiParam(name = "JSON body", required = true) @Valid @NotNull KinesisHealthCheckRequest heathCheckRequest) throws ExecutionException, IOException {
        HealthCheckResponse response = kinesisService.healthCheck(heathCheckRequest);
        return Response.accepted().entity(response).build();
    }
}