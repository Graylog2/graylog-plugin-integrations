package org.graylog.integrations.dataadapters;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.graylog.integrations.dataadpaters.GreyNoiseDataAdapter;
import org.graylog2.plugin.lookup.LookupResult;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class GreyNoiseDataAdapterTest {

    Response mockResponse;
    LookupResult result = null;
    String stringResponse;

    @Before
    public void setUp() throws Exception {

        stringResponse = "{\n" +
                         "\"code\":\"0x00\"\n" +
                         "\"ip\":\"string\"\n" +
                         "\"noise\":true\n" +
                         "}";

        Request mockRequest = new Request.Builder()
                .url("https://api.greynoise.io/v2/noise/quick/")
                .build();

        mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("")
                .body(ResponseBody.create(MediaType.get("application/json"), stringResponse))
                .build();

    }

    @Test
    public void parseBodyWithMultiValue() throws Exception {

        // TODO resolve error
        result = GreyNoiseDataAdapter.parseResponse(mockResponse);
        assertThat(result, notNullValue());
    }


}
