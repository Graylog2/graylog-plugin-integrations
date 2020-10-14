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
import com.github.joschi.jadconfig.util.Duration;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.graylog.events.notifications.*;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.integrations.notifications.modeldata.BacklogItemModelData;
import org.graylog.integrations.notifications.modeldata.StreamModelData;
import org.graylog.scheduler.JobTriggerDto;
import org.graylog2.jackson.TypeReferences;
import org.graylog2.notifications.Notification;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.shared.bindings.providers.ObjectMapperProvider;
import org.graylog2.shared.bindings.providers.OkHttpClientProvider;
import org.graylog2.streams.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;

public class SlackEventNotification implements EventNotification {

	private static final String UNKNOWN_VALUE = "<unknown>";
	private static final boolean DEV_PROFILE = true;


	public interface Factory extends EventNotification.Factory {
		@Override
        SlackEventNotification create();
	}


	private static final Logger LOG = LoggerFactory.getLogger(SlackEventNotification.class);

	EventNotificationService notificationCallbackService = null;
	StreamService streamService = null;
	Engine templateEngine = null;
	NotificationService notificationService = null;
	ObjectMapper objectMapper = null;
	NodeId nodeId = null;
	OkHttpClientProvider okHttpClientProvider = null;

	public SlackEventNotification() {
		objectMapper = new ObjectMapperProvider().get();
	}


	@Inject
	public SlackEventNotification(EventNotificationService notificationCallbackService,
                                  StreamService streamService,
                                  Engine templateEngine,
                                  NotificationService notificationService,
                                  ObjectMapper objectMapper,
								  OkHttpClientProvider okHttpClientProvider,
                                  NodeId nodeId) {
		this.notificationCallbackService = notificationCallbackService;
		this.streamService = streamService;
		this.templateEngine = templateEngine;
		this.notificationService = notificationService;
		this.objectMapper = objectMapper;
		this.nodeId = nodeId;
		this.okHttpClientProvider = okHttpClientProvider;

	}

	@Override
	public void execute(EventNotificationContext ctx) throws PermanentEventNotificationException {
		final SlackEventNotificationConfig config = (SlackEventNotificationConfig) ctx.notificationConfig();
		// TODO: 9/8/20  - use this.slackClient
        //
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
		boolean hasTemplate = !isNullOrEmpty(template);
		if (hasTemplate) {
			customMessage = buildCustomMessage(ctx, config, template);
		}

		List<String> backlogItemMessages = Collections.emptyList();
		String backlogItemTemplate = config.backlogItemMessage();
		boolean hasBacklogItemTemplate = !isNullOrEmpty(backlogItemTemplate);
		if(hasBacklogItemTemplate) {
			backlogItemMessages = buildBacklogItemMessages(ctx, config, backlogItemTemplate);
			LOG.info("The size of backlog Item Messages "+backlogItemMessages.size());
		}

		return new SlackMessage(
				config.color(),
				config.iconEmoji(),
				config.iconUrl(),
				config.userName(),
				config.channel(),
				linkNames,
				message,
				customMessage,
				backlogItemMessages);
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

	List<String> buildBacklogItemMessages(EventNotificationContext ctx, SlackEventNotificationConfig config, String template) {
		return getAlarmBacklog(ctx).stream()
				.map(backlogItem -> {
					Map<String, Object> model = getBacklogItemModel(ctx, config, backlogItem);
					try {
						String str =  templateEngine.transform(template, model);
						LOG.info("String build a backlog item message="+str);
						return str;
					} catch (Exception e) {
						LOG.error("Exception during templating", e);
						return e.toString();
					}
				}).collect(Collectors.toList());
	}

	List<MessageSummary> getAlarmBacklog(EventNotificationContext ctx) {
		if(DEV_PROFILE == false)
			return notificationCallbackService.getBacklogForEvent(ctx);

		Message msg = new Message("pallavi","geetham1", Tools.nowUTC());
		MessageSummary summary = new MessageSummary("1",msg);
		ArrayList<MessageSummary> summaries = new ArrayList<>();
		summaries.add(summary);
		return summaries;
	}

	Map<String, Object> getCustomMessageModel(EventNotificationContext ctx, SlackEventNotificationConfig config, List<MessageSummary> backlog) {
		Optional<EventDefinitionDto> definitionDto = ctx.eventDefinition();
		EventNotificationModelData modelData = EventNotificationModelData.builder()
								 			  .eventDefinitionId(definitionDto.map(EventDefinitionDto::id).orElse(UNKNOWN_VALUE))
				.eventDefinitionType(definitionDto.map(d -> d.config().type()).orElse(UNKNOWN_VALUE))
				.eventDefinitionTitle(definitionDto.map(EventDefinitionDto::title).orElse(UNKNOWN_VALUE))
				.eventDefinitionDescription(definitionDto.map(EventDefinitionDto::description).orElse(UNKNOWN_VALUE))
				.jobDefinitionId(ctx.jobTrigger().map(JobTriggerDto::jobDefinitionId).orElse(UNKNOWN_VALUE))
				.jobTriggerId(ctx.jobTrigger().map(JobTriggerDto::id).orElse(UNKNOWN_VALUE))
				.event(ctx.event())
				.backlog(backlog)
				.build();

		System.out.println(modelData.toString());

		Map<String, Object> objectMap = objectMapper.convertValue(modelData, TypeReferences.MAP_STRING_OBJECT);

		objectMap.put("graylog_url",isNullOrEmpty(config.graylogUrl()) ? UNKNOWN_VALUE : config.graylogUrl());

		//todo: what happens with eventdefinition in pipeline rules, what are it attributes ?
		//
		objectMap.put("event_definition", isNull(definitionDto) ? UNKNOWN_VALUE:definitionDto);

		if(streamService != null) {
			List<StreamModelData> streams  = streamService.loadByIds(ctx.event().sourceStreams())
					.stream()
					.map(stream -> buildStreamWithUrl(stream, ctx, config))
					.collect(Collectors.toList());
			objectMap.put("streams",isNull(streams) ? UNKNOWN_VALUE : streams);
		}

		return objectMap;
	}


	Map<String, Object> getBacklogItemModel(EventNotificationContext ctx, SlackEventNotificationConfig config, MessageSummary backlogItem) {
		Optional<EventDefinitionDto> definitionDto = ctx.eventDefinition();

		List<StreamModelData> streams = streamService.loadByIds(ctx.event().sourceStreams())
				.stream()
				.map(stream -> buildStreamWithUrl(stream, ctx, config))
				.collect(Collectors.toList());

		BacklogItemModelData modelData = BacklogItemModelData.builder()
				.eventDefinition(definitionDto)
				.eventDefinitionId(definitionDto.map(EventDefinitionDto::id).orElse(UNKNOWN_VALUE))
				.eventDefinitionType(definitionDto.map(d -> d.config().type()).orElse(UNKNOWN_VALUE))
				.eventDefinitionTitle(definitionDto.map(EventDefinitionDto::title).orElse(UNKNOWN_VALUE))
				.eventDefinitionDescription(definitionDto.map(EventDefinitionDto::description).orElse(UNKNOWN_VALUE))
				.jobDefinitionId(ctx.jobTrigger().map(JobTriggerDto::jobDefinitionId).orElse(UNKNOWN_VALUE))
				.jobTriggerId(ctx.jobTrigger().map(JobTriggerDto::id).orElse(UNKNOWN_VALUE))
				.event(ctx.event())
				.backlogItem(backlogItem)
				.graylogUrl(isNullOrEmpty(config.graylogUrl()) ? UNKNOWN_VALUE : config.graylogUrl())
				.streams(streams)
				.build();

		return objectMapper.convertValue(modelData, TypeReferences.MAP_STRING_OBJECT);
	}

	StreamModelData buildStreamWithUrl(Stream stream, EventNotificationContext ctx, SlackEventNotificationConfig config) {
		String graylogUrl = config.graylogUrl();
		String streamUrl = null;
		if(!isNullOrEmpty(graylogUrl)) {
			streamUrl = StringUtils.appendIfMissing(graylogUrl, "/") + "streams/" + stream.getId() + "/search";

			if(ctx.eventDefinition().isPresent()) {
				EventDefinitionDto eventDefinitionDto = ctx.eventDefinition().get();
				if(eventDefinitionDto.config() instanceof AggregationEventProcessorConfig) {
					String query = ((AggregationEventProcessorConfig) eventDefinitionDto.config()).query();
					streamUrl += "?q=" + query;
				}
			}
		}

		return StreamModelData.builder()
				.id(stream.getId())
				.title(stream.getTitle())
				.description(stream.getDescription())
				.url(Optional.ofNullable(streamUrl).orElse(UNKNOWN_VALUE))
				.build();
	}


}
