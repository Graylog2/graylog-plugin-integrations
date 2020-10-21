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
package org.graylog.integrations.pagerduty;

import org.graylog.events.notifications.EventNotification;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.events.notifications.EventNotificationException;
import org.graylog.integrations.pagerduty.client.PagerDutyClient;
import org.graylog.integrations.pagerduty.client.PagerDutyClient.PagerDutyClientException;
import org.graylog.integrations.pagerduty.dto.PagerDutyResponse;

import javax.inject.Inject;
import java.util.List;

/**
 * Main class that focuses on event notifications that should be sent to PagerDuty.
 *
 * @author Edgar Molina
 *
 */
public class PagerDutyNotification implements EventNotification
{
    private final PagerDutyClient pagerDutyClient;

    @Inject
    PagerDutyNotification(PagerDutyClient pagerDutyClient) {
        this.pagerDutyClient = pagerDutyClient;
    }

    public interface Factory extends EventNotification.Factory {
        @Override
        PagerDutyNotification create();
    }

    @Override
    public void execute(EventNotificationContext ctx) throws EventNotificationException {
        try {
            PagerDutyResponse response = pagerDutyClient.trigger(ctx);
            List<String> errors = response.getErrors();
            if (errors != null && errors.size() > 0) {
                throw new IllegalStateException(
                        "There was an error triggering the PagerDuty event, details: " + errors);
            }
        } catch (PagerDutyClientException e) {
            throw new EventNotificationException (
                    "There was an exception triggering the PagerDuty event.", e);
        } catch (Exception e) {
            throw new EventNotificationException (
                    "There was an exception triggering the PagerDuty event.", e);
        }
    }

}