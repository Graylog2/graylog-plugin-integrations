package org.graylog.integrations.pagerduty;

import org.assertj.core.util.Lists;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.events.notifications.EventNotificationException;
import org.graylog.integrations.pagerduty.client.PagerDutyClient;
import org.graylog.integrations.pagerduty.dto.PagerDutyResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PagerDutyNotificationTest {

    // Code Under Test
    @InjectMocks
    private PagerDutyNotification cut;

    // Mock Objects
    @Mock
    PagerDutyClient mockPagerDutyClient;

    // Test Objects
    EventNotificationContext ctx;

    // Test Cases
    @Test
    public void execute_runsWithoutErrors_whenFoo() throws Exception {
        givenGoodContext();
        givenPagerDutyClientSucceeds();

        whenExecuteIsCalled();

        thenPagerDutyClientIsInvokedOnce();
    }

    @Test(expected = EventNotificationException.class)
    public void execute_throwsEventNotificationException_whenClientThrowsPagerDutyClientException() throws Exception {
        givenGoodContext();
        givenPagerDutyClientThrowsPagerDutyClientException();

        whenExecuteIsCalled();
    }

    @Test(expected = EventNotificationException.class)
    public void execute_throwsEventNotificationException_whenClientThrowsIOException() throws Exception {
        givenGoodContext();
        givenPagerDutyClientThrowsRuntimeException();

        whenExecuteIsCalled();
    }

    // GIVENs
    private void givenGoodContext() {
        ctx = mock(EventNotificationContext.class);
    }

    private void givenPagerDutyClientSucceeds() throws Exception {
        PagerDutyResponse response = mock(PagerDutyResponse.class);
        given(response.getErrors()).willReturn(Lists.emptyList());
        given(mockPagerDutyClient.trigger(ctx)).willReturn(response);
    }

    private void givenPagerDutyClientThrowsPagerDutyClientException() throws Exception {
        given(mockPagerDutyClient.trigger(ctx)).willThrow(new PagerDutyClient.PagerDutyClientException("test"));
    }

    private void givenPagerDutyClientThrowsRuntimeException() throws Exception {
        given(mockPagerDutyClient.trigger(ctx)).willThrow(new RuntimeException("test"));
    }

    // WHENs
    private void whenExecuteIsCalled() throws EventNotificationException {
        cut.execute(ctx);
    }

    // THENs
    private void thenPagerDutyClientIsInvokedOnce() throws Exception {
        ArgumentCaptor<EventNotificationContext> contextCaptor = ArgumentCaptor.forClass(EventNotificationContext.class);
        verify(mockPagerDutyClient, times(1)).trigger(contextCaptor.capture());

        assertThat(contextCaptor.getValue(), notNullValue());
        assertThat(contextCaptor.getValue(), is(ctx));
    }
}
