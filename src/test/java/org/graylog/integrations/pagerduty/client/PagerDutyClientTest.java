package org.graylog.integrations.pagerduty.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.integrations.pagerduty.dto.PagerDutyMessage;
import org.graylog.integrations.pagerduty.dto.PagerDutyResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PagerDutyClientTest {

    // Code Under Test
    @InjectMocks
    private PagerDutyClient cut;

    // Mock Objects
    @Mock
    private OkHttpClient mockHttpClient;

    @Spy
    private ObjectMapper spyObjectMapper;

    @Mock
    private MessageFactory mockMessageFactory;

    // Test Objects
    private EventNotificationContext ctx;
    private PagerDutyResponse pagerDutyResponse;

    // Test constants
    private static final String TEST_MESSAGE = "This is a test of the Emergency Broadcast System.";
    private static final String GOOD_API_RESPONSE = "{\"status\":\"\", \"message\":\"\", \"dedup_key\":\"\", \"errors\":[]}";
    private static final String ERROR_API_RESPONSE = "{\"status\":\"\", \"message\":\"\", \"dedup_key\":\"\", \"errors\":[\"error\"]}";

    // Test Cases
    @Test
    public void trigger_returnsSuccessfulResponse_whenAPICallSucceeds() throws Exception {
        givenGoodMessageFactory();
        givenGoodContext();
        givenGoodObjectMapper();
        givenApiCallSucceeds();

        whenTriggerIsCalled();

        thenGoodRequestSentToAPI();
        thenGoodResponseReturned();
    }

    @Test
    public void trigger_returnsErrorResponse_whenAPICallReturnsErrors() throws Exception {
        givenGoodMessageFactory();
        givenGoodContext();
        givenGoodObjectMapper();
        givenApiCallReturnsErrors();

        whenTriggerIsCalled();

        thenGoodRequestSentToAPI();
        thenErrorResponseReturned();
    }

    @Test(expected = PagerDutyClient.PagerDutyClientException.class)
    public void trigger_throwsPagerDutyClientException_whenAPICallFails() throws Exception {
        givenGoodMessageFactory();
        givenGoodContext();
        givenGoodObjectMapper();
        givenApiCallFails();

        whenTriggerIsCalled();
    }

    // GIVENs
    private void givenGoodMessageFactory() {
        PagerDutyMessage message = mock(PagerDutyMessage.class);
        given(mockMessageFactory.createTriggerMessage(any(EventNotificationContext.class))).willReturn(message);
    }

    private void givenGoodContext() {
        ctx = mock(EventNotificationContext.class);
    }

    private void givenGoodObjectMapper() throws Exception {
        given(spyObjectMapper.writeValueAsString(any(PagerDutyMessage.class))).willReturn(TEST_MESSAGE);
    }

    private void givenApiCallSucceeds() throws Exception {
        Response response = buildResponse(true, GOOD_API_RESPONSE);
        Call mockCall = mock(Call.class);
        given(mockCall.execute()).willReturn(response);
        given(mockHttpClient.newCall(any(Request.class))).willReturn(mockCall);
    }

    private void givenApiCallReturnsErrors() throws Exception {
        Response response = buildResponse(true, ERROR_API_RESPONSE);
        Call mockCall = mock(Call.class);
        given(mockCall.execute()).willReturn(response);
        given(mockHttpClient.newCall(any(Request.class))).willReturn(mockCall);
    }

    private void givenApiCallFails() throws Exception {
        Response response = buildResponse(false, "");
        Call mockCall = mock(Call.class);
        given(mockCall.execute()).willReturn(response);
        given(mockHttpClient.newCall(any(Request.class))).willReturn(mockCall);
    }

    // WHENs
    private void whenTriggerIsCalled() throws Exception {
        pagerDutyResponse = cut.trigger(ctx);
    }

    // THENs
    private void thenGoodRequestSentToAPI() throws Exception {
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mockHttpClient, times(1)).newCall(requestCaptor.capture());

        assertThat(requestCaptor.getValue(), notNullValue());
        Request request = requestCaptor.getValue();

        assertThat(request.url().toString(), is(PagerDutyClient.API_URL));
        assertThat(request.method(), is("POST"));
        assertThat(request.body(), notNullValue());
        assertThat(request.body().contentLength(), is(Long.valueOf(TEST_MESSAGE.length())));
    }

    private void thenGoodResponseReturned() {
        assertThat(pagerDutyResponse, notNullValue());
        assertThat(pagerDutyResponse.getErrors().size(), is(0));
    }

    private void thenErrorResponseReturned() {
        assertThat(pagerDutyResponse, notNullValue());
        assertThat(pagerDutyResponse.getErrors().size(), is(1));
    }

    // Utility Methods
    private Response buildResponse(boolean httpSuccess, String responseBody) {
        int httpCode = 404;

        if (httpSuccess) {
            httpCode = 200;
        }

        return new Response.Builder()
                .request(new Request.Builder()
                        .url("https://events.pagerduty.com/v2/enqueue")
                        .build())
                .protocol(Protocol.HTTP_2)
                .code(httpCode)
                .message("")
                .body(ResponseBody.create(MediaType.parse(APPLICATION_JSON), responseBody))
                .build();
    }
}
