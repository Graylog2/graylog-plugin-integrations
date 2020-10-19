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
package org.graylog.integrations.notifications.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floreysoft.jmte.Engine;
import org.apache.directory.api.util.Strings;
import org.graylog.events.notifications.*;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.integrations.notifications.modeldata.StreamModelData;
import org.graylog.scheduler.JobTriggerDto;
import org.graylog2.jackson.TypeReferences;
import org.graylog2.notifications.Notification;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.shared.bindings.providers.OkHttpClientProvider;
import org.graylog2.streams.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class SlackEventNotification implements EventNotification {

	private static final String UNKNOWN_VALUE = "<unknown>";

	public interface Factory extends EventNotification.Factory {
		@Override
        SlackEventNotification create();
	}


	private static final Logger LOG = LoggerFactory.getLogger(SlackEventNotification.class);

	private final EventNotificationService notificationCallbackService;
	private final Optional<StreamService> streamService ;
	private final Engine templateEngine ;
	private final NotificationService notificationService ;
	private final ObjectMapper objectMapper ;
	private final NodeId nodeId ;
	private final OkHttpClientProvider okHttpClientProvider ;


	@Inject
	public SlackEventNotification(EventNotificationService notificationCallbackService,
								  ObjectMapper objectMapper,
								  Engine templateEngine,
								  NotificationService notificationService,
								  OkHttpClientProvider okHttpClientProvider,
								  NodeId nodeId, StreamService streamService){
		this.notificationCallbackService = notificationCallbackService;
		this.objectMapper = requireNonNull(objectMapper);
		this.templateEngine = requireNonNull(templateEngine);
		this.okHttpClientProvider = requireNonNull(okHttpClientProvider);
		this.notificationService = requireNonNull(notificationService);
		this.nodeId = requireNonNull(nodeId);
		this.streamService = Optional.ofNullable(streamService);

	}


	@Override
	public void execute(EventNotificationContext ctx) throws PermanentEventNotificationException {
		final SlackEventNotificationConfig config = (SlackEventNotificationConfig) ctx.notificationConfig();
		SlackClient slackClient = new SlackClient(config,okHttpClientProvider.get());

		try {
			SlackMessage slackMessage = createSlackMessage(ctx, config);
			slackClient.send_with_okhttp(slackMessage);
		} catch (Exception e) {
			String exceptionDetail = e.toString();
			if (e.getCause() != null) {
				exceptionDetail += " (" + e.getCause() + ")";
			}


			final Notification systemNotification = notificationService.buildNow()
					.addNode(nodeId.toString())
					.addType(Notification.Type.GENERIC)
					.addSeverity(Notification.Severity.NORMAL)
					.addDetail("exception", exceptionDetail);
			notificationService.publishIfFirst(systemNotification);

			throw new PermanentEventNotificationException("Slack notification is triggered, but sending failed. " + e.getMessage(), e);
		}
	}

	SlackMessage createSlackMessage(EventNotificationContext ctx, SlackEventNotificationConfig config) {
		//Note: Link names if notify channel or else the channel tag will be plain text.
		boolean linkNames = config.linkNames() || config.notifyChannel();
		String message = buildDefaultMessage(ctx, config);

		String customMessage = null;
		String template = config.customMessage();
		if (!isNullOrEmpty(template)) {
			customMessage = buildCustomMessage(ctx, config, template);
		}

		return new SlackMessage(
				config.color(),
				config.iconEmoji(),
				config.iconUrl(),
				config.userName(),
				config.channel(),
				linkNames,
				message,
				customMessage
				);
	}

	String buildDefaultMessage(EventNotificationContext ctx, SlackEventNotificationConfig config) {
		String title = buildMessageTitle(ctx, config);

		// Build custom message
		String audience = config.notifyChannel() ? "@channel " : "";
		String description = ctx.eventDefinition().map(EventDefinitionDto::description).orElse("");
		return String.format(Locale.ROOT,"%s*Alert %s* triggered:\n> %s \n", audience, title, description);
	}

	private String buildMessageTitle(EventNotificationContext ctx, SlackEventNotificationConfig config) {
		String graylogUrl = config.graylogUrl();
		String eventDefinitionName = ctx.eventDefinition().map(EventDefinitionDto::title).orElse("Unnamed");
		if(!isNullOrEmpty(graylogUrl)) {
			return "<" + graylogUrl + "|" + eventDefinitionName + ">";
		} else {
			return "_" + eventDefinitionName + "_";
		}
	}

	String buildCustomMessage(EventNotificationContext ctx, SlackEventNotificationConfig config, String template) {
		List<MessageSummary> backlog = getAlarmBacklog(ctx);
		Map<String, Object> model = getCustomMessageModel(ctx, config, backlog);
		try {
			return templateEngine.transform(template, model);
		} catch (Exception e) {
			LOG.error("Exception during templating", e);
			return e.toString();
		}
	}


	List<MessageSummary> getAlarmBacklog(EventNotificationContext ctx) {
		return notificationCallbackService.getBacklogForEvent(ctx);
	}

	Map<String, Object> getCustomMessageModel(EventNotificationContext ctx, SlackEventNotificationConfig config, List<MessageSummary> backlog) {
		Optional<EventDefinitionDto> definitionDto = ctx.eventDefinition();
		EventNotificationModelData modelData = EventNotificationModelData.builder()
								 			  .eventDefinitionId(definitionDto.map(EventDefinitionDto::id).orElse(UNKNOWN_VALUE))
				.eventDefinitionType(config.type())
				.eventDefinitionTitle(definitionDto.map(EventDefinitionDto::title).orElse(UNKNOWN_VALUE))
				.eventDefinitionDescription(definitionDto.map(EventDefinitionDto::description).orElse(UNKNOWN_VALUE))
				.jobDefinitionId(ctx.jobTrigger().map(JobTriggerDto::jobDefinitionId).orElse(UNKNOWN_VALUE))
				.jobTriggerId(ctx.jobTrigger().map(JobTriggerDto::id).orElse(UNKNOWN_VALUE))
				.event(ctx.event())
				.backlog(backlog)
				.build();

		LOG.debug("the custom message model data is {}",modelData.toString());
		Map<String, Object> objectMap = objectMapper.convertValue(modelData, TypeReferences.MAP_STRING_OBJECT);
		objectMap.put("graylog_url",isNullOrEmpty(config.graylogUrl()) ? UNKNOWN_VALUE : config.graylogUrl());
		//Q:what is the purpose of the eventdefinition in pipeline rules, what are it attributes ?
		//
		objectMap.put("event_definition", isNull(definitionDto) ? UNKNOWN_VALUE:definitionDto);
		streamService.ifPresent(theStream -> getObjectMap(ctx, config, objectMap));
		return objectMap;
	}



	StreamModelData buildStreamWithUrl(Stream stream, EventNotificationContext ctx, SlackEventNotificationConfig config) {
		String graylogUrl = config.graylogUrl();
		Optional<String> streamUrl  = getStreamUrl(stream, ctx, graylogUrl);
		LOG.debug("streamUrl is {}",streamUrl);

		return StreamModelData.builder()
				.id(stream.getId())
				.title(stream.getTitle())
				.description(stream.getDescription())
				.url(Optional.ofNullable(streamUrl).orElse(Optional.of(UNKNOWN_VALUE)))
				.build();
	}


	Optional<String> getStreamUrl(Stream stream, EventNotificationContext ctx, String graylogUrl) {
		StringBuffer streamUrl = new StringBuffer();
		if (Strings.isEmpty(graylogUrl)) {
			streamUrl.append(graylogUrl)
					.append("streams/")
					.append(stream.getId())
					.append("/search");
			if (ctx.eventDefinition().isPresent()
					&& ctx.eventDefinition().get().config() instanceof AggregationEventProcessorConfig
					&& !((AggregationEventProcessorConfig) ctx.eventDefinition().get().config()).query().isEmpty()) {
					AggregationEventProcessorConfig conf = (AggregationEventProcessorConfig) ctx.eventDefinition().get().config();
					streamUrl.append("?q=")
					.append(conf.query());
			}
			return Optional.of(streamUrl.toString());
		}
		return Optional.empty();
	}


}
