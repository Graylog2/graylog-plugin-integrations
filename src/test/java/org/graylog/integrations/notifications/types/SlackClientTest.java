package org.graylog.integrations.notifications.types;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SlackClientTest {

    private final MockWebServer server = new MockWebServer();
    private OkHttpClient mockHttpClient;


    @Before
    public void setUp() throws Exception {
       mockHttpClient = getMockHttpClient("{\"key\": \"val\"}",200);
       server.start();
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
        mockHttpClient = null;
    }



    @Test
    public void send_is_successful() throws Exception {
        SlackClient slackClient = new SlackClient(mockHttpClient);
        SlackMessage message = new SlackMessage("Henry Hühnchen(little chicken)");
        slackClient.send(message,"http://url.com");
    }

    private static OkHttpClient getMockHttpClient(final String serializedBody,int httpCode) throws IOException {
        final OkHttpClient okHttpClient = mock(OkHttpClient.class);

        final Call remoteCall = mock(Call.class);

        final Response response = new Response.Builder()
                .request(new Request.Builder().url("http://url.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(httpCode).message("").body(
                        ResponseBody.create(
                                MediaType.parse("application/json"),
                                serializedBody
                        ))
                .build();

        when(remoteCall.execute()).thenReturn(response);
        when(okHttpClient.newCall(any())).thenReturn(remoteCall);

        return okHttpClient;
    }




}
