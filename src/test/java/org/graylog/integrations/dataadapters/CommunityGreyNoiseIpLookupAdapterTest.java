package org.graylog.integrations.dataadapters;

import com.unboundid.util.json.JSONException;
import com.unboundid.util.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.graylog2.plugin.lookup.LookupResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class CommunityGreyNoiseIpLookupAdapterTest {

    private Request request;

    @Before
    public void setup() {

        request = new Request.Builder()
                .url(CommunityGreyNoiseIpLookupAdapter.GREYNOISE_ENDPOINT)
                .build();
    }

    @Test public void testParseSuccess(){

        String string = "{\"ip\": \"1.2.3.4\",\"noise\": false,\"riot\": true,\"classification\": \"benign\",\"name\": \"Cloudflare\",\"link\": \"https://viz.greynoise.io/riot/1.2.3.4\",\"last_seen\": \"2020-01-01\",\"message\": \"Success\"}";
        int statusCode = 200;

        Response response = createResponse(string, statusCode);
        LookupResult result = CommunityGreyNoiseIpLookupAdapter.parseResponse(response);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse("Result for status 200 not expected to have errors",result.hasError());
        assertValidMapWithKeys(result.multiValue(), "ip", "noise", "riot", "message","classification","name","link","last_seen","message");
    }

    @Test
    public void testParse404() {

        String string = "{\"ip\": \"1.2.3.4\",\"noise\": false,\"riot\": false,\"message\": \"IP not observed scanning the internet or contained in RIOT data set.\"}";
        int statusCode = 404;

        Response response = createResponse(string, statusCode);
        LookupResult result = CommunityGreyNoiseIpLookupAdapter.parseResponse(response);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());

        assertValidMapWithKeys(result.multiValue(), "ip", "noise", "riot", "message");

        Assert.assertFalse("Invalid Result. A 404 response should not be considered an error",result.hasError());
    }

    @Test public void testRateLimitReached(){

        String string = "{\"plan\": \"unauthenticated\",\"rate-limit\": \"100-lookups/day\",\"plan_url\": \"https://greynoise.io/pricing\",\"message\": \"You have hit your daily rate limit of 100 requests per day. Please create a free account or upgrade your plan at https://greynoise.io/pricing.\"}";
        int statusCode = 429;

        Response response = createResponse(string, statusCode);
        LookupResult result = CommunityGreyNoiseIpLookupAdapter.parseResponse(response);

        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue("Result for status 429 expected to have errors",result.hasError());
        assertValidMapWithKeys(result.multiValue(), "plan","rate-limit","plan_url","message");
    }

    private Response createResponse(String string, int statusCode) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .code(statusCode)
                .message("")
                .body(ResponseBody.create(MediaType.get("application/json"), string))
                .build();
    }

    private void assertValidMapWithKeys(Map<Object, Object> map, Object... keys) {

        Assert.assertNotNull("Invalid result values", map);

        for (Object key : keys) {
            String error = String.format("Key [%s] not found in values map", key);
            Assert.assertTrue(error, map.containsKey(key));
        }
    }
}