package org.graylog.integrations.aws.service;

/**
 * Supports the ability to automatically parse
 */
public class AWSLogMessage {

    private static final String ACTION_ACCEPT = "ACCEPT";
    private static final String ACTION_REJECT = "REJECT";

    private String logMessage;

    public AWSLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    /**
     * Detects the type of log message.
     *
     * @return
     */
    public Type messageType() {

        // AWS Flow Logs
        // 2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 ACCEPT OK

        // Not using a regex here, because it would be quite complicated and hard to maintain.
        // Performance should not be an issue here, because this will only be executed once when detecting a log message.
        if ((logMessage.contains(ACTION_ACCEPT) || logMessage.contains(ACTION_REJECT)) &&
            logMessage.chars().filter(Character::isSpaceChar).count() == 13) {
            return Type.FLOW_LOGS;
        }

        // Add more log message types here as needed

        return Type.UNKNOWN;
    }

    // One enum value should be added for each type of log message that auto-detect is supported for.
    public enum Type {

        FLOW_LOGS, // See https://docs.aws.amazon.com/vpc/latest/userguide/flow-logs.html
        UNKNOWN
    }
}
