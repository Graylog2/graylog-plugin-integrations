package org.graylog.integrations.notifications.types;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.graylog.events.notifications.PermanentEventNotificationException;
import org.graylog.events.notifications.TemporaryEventNotificationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;


public class SlackClientTest {
    
    private OkHttpClient mockHttpClient;


    @Before
    public void setUp() throws Exception {
       mockHttpClient = getMockHttpClient("{\"key\": \"val\"}",200);
    }

    @After
    public void tearDown() throws IOException {
        mockHttpClient = null;
    }


    @Test
    public void send_sendsHttpRequestAsExpected_whenInputIsGood() throws Exception {
        SlackClient slackClient = new SlackClient(mockHttpClient);
        SlackMessage message = new SlackMessage("Henry Hühnchen(little chicken)");
        slackClient.send(message,"http://url.com");
        
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient, times(1)).newCall(requestCaptor.capture());
        
        assertThat(requestCaptor.getValue(), notNullValue());
        Request sent = requestCaptor.getValue();
        assertThat(sent.url().toString(), is("http://url.com"));
        assertThat(sent.method(), is("POST"));
        assertThat(sent.body(), notNullValue());
        assertThat(sent.body().contentLength(), is(Long.valueOf(message.length())));
    }

    @Test(expected = TemporaryEventNotificationException.class)
    public void send_throwsTempNotifException_whenHttpClientThrowsIOException() throws Exception {

        final OkHttpClient okHttpClient = mock(OkHttpClient.class);
        final Call remoteCall = mock(Call.class);
        when(remoteCall.execute()).thenThrow(new IOException("Request timeout"));
        when(okHttpClient.newCall(any())).thenReturn(remoteCall);


        SlackClient slackClient = new SlackClient(okHttpClient);
        SlackMessage message = new SlackMessage("Henry Hühnchen(little chicken)");
        slackClient.send(message,"http://url.com");
    }

    @Test(expected = PermanentEventNotificationException.class)
    public void send_throwsPermNotifException_whenPostReturnsHttp402() throws Exception {

        final OkHttpClient okHttpClient = getMockHttpClient("{\"key\": \"val\"}",402);
        SlackClient slackClient = new SlackClient(okHttpClient);
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
