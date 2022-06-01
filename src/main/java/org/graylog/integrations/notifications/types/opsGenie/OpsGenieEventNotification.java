/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package org.graylog.integrations.notifications.types.opsGenie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.floreysoft.jmte.Engine;
import com.google.common.annotations.VisibleForTesting;
import org.graylog.events.notifications.EventNotification;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog.events.notifications.EventNotificationException;
import org.graylog.events.notifications.EventNotificationModelData;
import org.graylog.events.notifications.EventNotificationService;
import org.graylog.events.notifications.PermanentEventNotificationException;
import org.graylog.events.notifications.TemporaryEventNotificationException;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog2.jackson.TypeReferences;
import org.graylog2.notifications.Notification;
import org.graylog2.notifications.NotificationService;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.system.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class OpsGenieEventNotification implements EventNotification{

    private static final Logger LOG = LoggerFactory.getLogger(OpsGenieEventNotification.class);
    private final EventNotificationService notificationCallbackService;
    private final Engine templateEngine;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final NodeId nodeId;
    private final OpsGenieClient requestClient;

    @Inject
    public OpsGenieEventNotification(EventNotificationService notificationCallbackService,
                                     ObjectMapper objectMapper,
                                     Engine templateEngine,
                                     NotificationService notificationService,
                                     NodeId nodeId, OpsGenieClient requestClient) {
        this.notificationCallbackService = notificationCallbackService;
        this.objectMapper = requireNonNull(objectMapper);
        this.templateEngine = requireNonNull(templateEngine);
        this.notificationService = requireNonNull(notificationService);
        this.nodeId = requireNonNull(nodeId);
        this.requestClient = requireNonNull(requestClient);
    }

    /**
     * @param ctx EventNotificationContext
     * @throws EventNotificationException is thrown when execute fails
     */
    @Override
    public void execute(EventNotificationContext ctx) throws EventNotificationException {
        final OpsGenieEventNotificationConfig config = (OpsGenieEventNotificationConfig) ctx.notificationConfig();
        LOG.debug("OpsGenieEventNotification backlog size in method execute is [{}]", config.backlogSize());

        try {
            OpsGenieMessage opsGenieMessage = createOpsGenieMessage(ctx, config);
            requestClient.send(opsGenieMessage.getJsonString(), config.accessToken());
        } catch (TemporaryEventNotificationException exp) {
            //scheduler needs to retry a TemporaryEventNotificationException
            throw exp;
        } catch (PermanentEventNotificationException exp) {
            String errorMessage = String.format("Error sending the OpsGeniesEventNotification :: %s", exp.getMessage());
            final Notification systemNotification = notificationService.buildNow()
                    .addNode(nodeId.toString())
                    .addType(Notification.Type.GENERIC)
                    .addSeverity(Notification.Severity.URGENT)
                    .addDetail("title", "OpsGenieEventNotification Failed")
                    .addDetail("description", errorMessage);

            notificationService.publishIfFirst(systemNotification);
            throw exp;

        } catch (Exception exp) {
            throw new EventNotificationException("There was an exception triggering the OpsGenieEventNotification", exp);
        }
    }

    /**
     * @param ctx EventNotificationContext
     * @param config custom configuration from user input
     * @return OpsGenie Message
     * @throws PermanentEventNotificationException - throws this exception when the custom message template is invalid
     */
    OpsGenieMessage createOpsGenieMessage(EventNotificationContext ctx, OpsGenieEventNotificationConfig config) throws PermanentEventNotificationException {
        //Note: Link names if notify channel or else the channel tag will be plain text.
        String template = config.customMessage();
        String mainMessageTemplate = config.mainFields();
        Map<String, String> mainMessage = buildCustomMessage(ctx,config,mainMessageTemplate);
        Map<String, String> customMessage = buildCustomMessage(ctx,config,template);
        String[] responders = config.channel().split(",");
        String priority = getEventPriority(ctx,mainMessage);

        String messageDescription = buildMessageDescription(ctx, mainMessage);
        String messageTitle = buildMessageTitle(ctx, mainMessage);

        String[] tags = config.tags().split(",");

        return new OpsGenieMessage(
                messageTitle,
                customMessage,
                messageDescription,
                config.userName(),
                tags,
                priority,
                responders
        );
    }

    String buildDefaultMessage(EventNotificationContext ctx) {
        String title = ctx.eventDefinition().map(EventDefinitionDto::title).orElse("Unnamed");
        // Build Message title
        return String.format(Locale.ROOT, "**Alert %s triggered:**\n", title);
    }

    private String buildMessageDescription(EventNotificationContext ctx, Map<String, String> mainMessage) {
        if (mainMessage.get("Message") != null) return mainMessage.get("Message");
        String description = ctx.eventDefinition().map(EventDefinitionDto::description).orElse("");
        return "_" + description + "_";
    }

    private String getEventPriority(EventNotificationContext ctx, Map<String, String> mainMessage){
        if (mainMessage.get("Priority") != null) return "P"+mainMessage.get("Priority");
        int priority = ctx.eventDefinition().map(EventDefinitionDto::priority).orElse(3);
        return "P"+priority;
    }


    private String buildMessageTitle(EventNotificationContext ctx, Map<String, String> mainMessage) {
        if (mainMessage.get("Title") != null) return mainMessage.get("Title");
        String eventDefinitionName = ctx.eventDefinition().map(EventDefinitionDto::title).orElse("Unnamed");
        return "_" + eventDefinitionName + "_";
    }

    Map<String, String> buildCustomMessage(EventNotificationContext ctx, OpsGenieEventNotificationConfig config, String template) throws PermanentEventNotificationException {
        final List<MessageSummary> backlog = getMessageBacklog(ctx, config);
        Map<String, Object> model = getCustomMessageModel(ctx, config.type(), backlog);

        try {
            String facts = templateEngine.transform(template, model);
            Map<String, String> events = getMessageDetails(facts);
            LOG.debug("customMessage: template = {} model = {}", template, model);
            return events;
        } catch (Exception e) {
            String error = "Invalid Custom Message template.";
            LOG.error(error + "[{}]", e.toString());
            throw new PermanentEventNotificationException(error + e, e.getCause());
        }
}

    public Map<String, String> getMessageDetails(String eventFields) {
        String[] fields = eventFields.split("\\r?\\n");
        Map<String,String> facts = new HashMap<>();

        for (String  field: fields) {
            String[] factFields = field.split(":");
            facts.put(factFields[0], factFields.length == 1 ? "" : factFields[1].trim());
        }
        LOG.debug("Created events for OpsGenie");
        return facts;

    }

    @VisibleForTesting
    List<MessageSummary> getMessageBacklog(EventNotificationContext ctx, OpsGenieEventNotificationConfig config) {
        List<MessageSummary> backlog = notificationCallbackService.getBacklogForEvent(ctx);
        if (config.backlogSize() > 0 && backlog != null) {
            return backlog.stream().limit(config.backlogSize()).collect(Collectors.toList());
        }
        return backlog;
    }


    @VisibleForTesting
    Map<String, Object> getCustomMessageModel(EventNotificationContext ctx, String type, List<MessageSummary> backlog) {
        EventNotificationModelData modelData = EventNotificationModelData.of(ctx, backlog);

        LOG.debug("the custom message model data is {}", modelData.toString());
        Map<String, Object> objectMap = objectMapper.convertValue(modelData, TypeReferences.MAP_STRING_OBJECT);
        objectMap.put("type", type);

        return objectMap;
    }

    public interface Factory extends EventNotification.Factory {
        @Override
        OpsGenieEventNotification create();
    }

}
