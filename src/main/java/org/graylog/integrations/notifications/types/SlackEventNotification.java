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
import org.graylog.events.notifications.*;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog2.jackson.TypeReferences;
import org.graylog2.notifications.Notification;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.rest.ValidationResult;
import org.graylog2.plugin.system.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


import java.util.List;
import java.util.Locale;
import java.util.Map;


import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;

public class SlackEventNotification implements EventNotification {

	private static final String UNKNOWN_VALUE = "<unknown>";

	public interface Factory extends EventNotification.Factory {
		@Override
        SlackEventNotification create();
	}


	private static final Logger LOG = LoggerFactory.getLogger(SlackEventNotification.class);

	private final EventNotificationService notificationCallbackService;
	private final Engine templateEngine ;
	private final NotificationService notificationService ;
	private final ObjectMapper objectMapper ;
	private final NodeId nodeId ;
	private final SlackClient slackClient;

	@Inject
	public SlackEventNotification(EventNotificationService notificationCallbackService,
								  ObjectMapper objectMapper,
								  Engine templateEngine,
								  NotificationService notificationService,
								  NodeId nodeId, SlackClient slackCLient){
		this.notificationCallbackService = notificationCallbackService;
		this.objectMapper = requireNonNull(objectMapper);
		this.templateEngine = requireNonNull(templateEngine);
		this.notificationService = requireNonNull(notificationService);
		this.nodeId = requireNonNull(nodeId);
		this.slackClient = requireNonNull(slackCLient);
	}

	/**
	 *
	 * @param ctx
	 * @throws PermanentEventNotificationException is thrown with bad webhook url, authentication error type issues
	 * @throws TemporaryEventNotificationException is thrown for network or timeout type issues
	 */
	@Override
	public void execute(EventNotificationContext ctx) throws PermanentEventNotificationException,TemporaryEventNotificationException {
		final SlackEventNotificationConfig config = (SlackEventNotificationConfig) ctx.notificationConfig();
		ValidationResult  result = config.validate();

		if(result.getErrors().size() > 0){
			throw new PermanentEventNotificationException("Please verify your webhook URL");
		}

		try {
			SlackMessage slackMessage = createSlackMessage(ctx, config);
			slackClient.send(slackMessage,config.webhookUrl());
		} catch (Exception e) {
			final Notification systemNotification = notificationService.buildNow()
					.addNode(nodeId.toString())
					.addType(Notification.Type.GENERIC)
					.addSeverity(Notification.Severity.NORMAL)
					.addDetail("exception", e.toString());

			notificationService.publishIfFirst(systemNotification);
			throw new TemporaryEventNotificationException("Slack notification is triggered, but sending failed. " + e.getMessage(), e);

		}

	}

	/**
	 *
	 * @param ctx
	 * @param config
	 * @return
	 * @throws PermanentEventNotificationException - throws this exception when the custom message template is invalid
	 */
	SlackMessage createSlackMessage(EventNotificationContext ctx, SlackEventNotificationConfig config) throws PermanentEventNotificationException {
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
		String title = buildMessageTitle(ctx);

		// Build custom message
		String audience = config.notifyChannel() ? "@channel " : "";
		String description = ctx.eventDefinition().map(EventDefinitionDto::description).orElse("");
		return String.format(Locale.ROOT,"%s*Alert %s* triggered:\n> %s \n", audience, title, description);
	}

	private String buildMessageTitle(EventNotificationContext ctx) {
		String eventDefinitionName = ctx.eventDefinition().map(EventDefinitionDto::title).orElse("Unnamed");
		return "_" + eventDefinitionName + "_";
	}

	String buildCustomMessage(EventNotificationContext ctx, SlackEventNotificationConfig config, String template) throws PermanentEventNotificationException {
		List<MessageSummary> backlog = getAlarmBacklog(ctx);
		Map<String, Object> model = getCustomMessageModel(ctx, config, backlog);
		try {
			LOG.debug("template = {} model = {}" ,template, model);
			return templateEngine.transform(template, model);
		} catch (Exception e) {
			LOG.error("Exception during templating", e);
			throw new PermanentEventNotificationException(e.toString(),e.getCause());
		}
	}


	List<MessageSummary> getAlarmBacklog(EventNotificationContext ctx) {
		return notificationCallbackService.getBacklogForEvent(ctx);
	}

	Map<String, Object> getCustomMessageModel(EventNotificationContext ctx, SlackEventNotificationConfig config, List<MessageSummary> backlog) {
		EventNotificationModelData modelData = EventNotificationModelData.of(ctx, backlog);

		LOG.debug("the custom message model data is {}",modelData.toString());
		Map<String, Object> objectMap = objectMapper.convertValue(modelData, TypeReferences.MAP_STRING_OBJECT);
		objectMap.put("type",config.type());
		return objectMap;
	}


}