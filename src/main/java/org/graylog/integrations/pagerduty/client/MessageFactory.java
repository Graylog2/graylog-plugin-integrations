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
package org.graylog.integrations.pagerduty.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.graylog.events.event.EventDto;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.integrations.pagerduty.PagerDutyNotificationConfig;
import org.graylog.integrations.pagerduty.dto.Link;
import org.graylog.integrations.pagerduty.dto.PagerDutyMessage;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;

import javax.inject.Inject;

/**
 * Factory class for PagerDuty messages, heavily based on the works of the cited authors.
 *
 * @author Jochen Schalanda
 * @author James Carr
 * @author Dennis Oelkers
 * @author Padma Liyanage
 * @author Edgar Molina
 */
class MessageFactory {
    private static final List<String> PAGER_DUTY_PRIORITIES = Arrays.asList("info", "warning", "critical");
    private final StreamService streamService;

    @Inject
    MessageFactory(StreamService streamService) {
        this.streamService = streamService;
    }

    PagerDutyMessage createTriggerMessage(EventNotificationContext ctx) {
        final EventDto event = ctx.event();
        final PagerDutyNotificationConfig config = (PagerDutyNotificationConfig) ctx.notificationConfig();
        String eventTitle = "Undefined";
        String eventPriority = PAGER_DUTY_PRIORITIES.get(0);

        if (ctx.eventDefinition().isPresent()) {
            eventTitle = ctx.eventDefinition().get().title();

            int priority = ctx.eventDefinition().get().priority() - 1;
            if (priority >= 0 && priority <= 2) {
                eventPriority = PAGER_DUTY_PRIORITIES.get(priority);
            }
        }

        List<Link> streamLinks =
                streamService
                        .loadByIds(event.sourceStreams())
                        .stream()
                        .map(stream -> buildStreamWithUrl(stream, ctx, config))
                        .collect(Collectors.toList());

        String dedupKey = "";
        if (config.customIncident()) {
            dedupKey = String.format(
                    "%s/%s/%s", config.keyPrefix(), event.sourceStreams(), eventTitle);
        }


        Map<String, String> payload = new HashMap<String, String>();
        payload.put("summary", event.message());
        payload.put("source", "Graylog:" + event.sourceStreams());
        payload.put("severity", eventPriority);
        payload.put("timestamp", event.eventTimestamp().toString());
        payload.put("component", "GraylogAlerts");
        payload.put("group", event.sourceStreams().toString());
        payload.put("class", "alerts");

        return new PagerDutyMessage(
                config.routingKey(),
                "trigger",
                dedupKey,
                config.clientName(),
                config.clientUrl(),
                streamLinks,
                payload);
    }

    private Link buildStreamWithUrl(Stream stream, EventNotificationContext ctx, PagerDutyNotificationConfig config) {
        final String graylogUrl = config.clientUrl();
        String streamUrl =
                StringUtils.appendIfMissing(graylogUrl, "/") + "streams/" + stream.getId() + "/search";

        if (ctx.eventDefinition().isPresent()) {
            EventDefinitionDto eventDefinitionDto = ctx.eventDefinition().get();
            if (eventDefinitionDto.config() instanceof AggregationEventProcessorConfig) {
                String query =
                        ((AggregationEventProcessorConfig) eventDefinitionDto.config()).query();
                streamUrl += "?q=" + query;
            }
        }
        try {
            return new Link(new URL(streamUrl), stream.getTitle());
        }
        catch (MalformedURLException e) {
            throw new IllegalStateException("Error when building the stream link URL.", e);
        }
    }
}