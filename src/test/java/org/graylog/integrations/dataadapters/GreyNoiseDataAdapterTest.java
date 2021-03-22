package org.graylog.integrations.dataadapters;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.assertj.core.api.Assertions;
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

        stringResponse = "{\"ip\":\"192.168.1.1\",\"noise\":true,\"code\":\"0x01\"}";

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

        result = GreyNoiseDataAdapter.parseResponse(mockResponse);
        assertThat(result, notNullValue());
        Assertions.assertThat(result.isEmpty()).isFalse();
        Assertions.assertThat(result.hasError()).isFalse();
        Assertions.assertThat(result.singleValue()).isEqualTo(null);
        Assertions.assertThat(result.multiValue()).isNotNull();
        Assertions.assertThat(result.multiValue().containsValue("192.168.1.1")).isTrue();
        Assertions.assertThat(result.multiValue().containsValue("0x01")).isTrue();
        Assertions.assertThat(result.multiValue().containsValue(true)).isTrue();
    }


}
