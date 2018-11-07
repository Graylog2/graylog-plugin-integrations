package org.graylog.plugins.integrations.outputs;

import java.util.List;
import java.util.Map;

public class BatchedRequest {

    public BatchedRequest() { }

    public BatchedRequest(List<Map<String, Object>> messages) {
        this.messages = messages;
    }

    public List<Map<String, Object>> messages;

}
