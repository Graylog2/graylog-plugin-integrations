package org.graylog.integrations.okta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.graylog2.inputs.InputService;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.shared.inputs.MessageInputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OktaService {

    private static final Logger LOG = LoggerFactory.getLogger(OktaService.class);

    private final InputService inputService;
    private final MessageInputFactory messageInputFactory;
    private final NodeId nodeId;
    private final ObjectMapper objectMapper;

    @Inject
    public OktaService(InputService inputService, MessageInputFactory messageInputFactory, NodeId nodeId,
                       ObjectMapper objectMapper) {

        this.inputService = inputService;
        this.messageInputFactory = messageInputFactory;
        this.nodeId = nodeId;
        this.objectMapper = objectMapper;
    }
}