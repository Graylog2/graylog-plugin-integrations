package org.graylog.integrations.aws.service;

import org.graylog.integrations.aws.codec.CloudWatchFlowLogCodec;
import org.graylog.integrations.aws.codec.CloudWatchRawLogCodec;

/**
 * Supports the ability to automatically parse
 */
public class AWSLogMessage {

    private String logMessage;

    public AWSLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    /**
     * Detects the type of log message.
     *
     * @return A {@code Type} indicating the which kind of log message has been detected.
     */
    public Type detectLogMessageType() {

        if (isFlowLog()) {
            return Type.FLOW_LOGS;
        }

        return Type.UNKNOWN;
    }

    /**
     * Flow logs are space-delimited messages. See https://docs.aws.amazon.com/vpc/latest/userguide/flow-logs.html
     *
     * Sample: 2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 ACCEPT OK
     *
     * Match a message with exactly 13 spaces and either the word ACCEPT or REJECT.
     * Use simple if checks instead of regex to keep this simple. Performance should not be a concern, since
     * this is only called once during the healthcheck.
     *
     * @return true if message is a flowlog.
     */
    public boolean isFlowLog() {
        boolean hasAction = logMessage.contains("ACCEPT") || logMessage.contains("REJECT");
        long spaceCount = logMessage.chars().filter(Character::isSpaceChar).count();

        return hasAction && spaceCount == 13;
    }

    // One enum value should be added for each type of log message that auto-detect is supported for.
    public enum Type {

        FLOW_LOGS("AWS Flow Log", CloudWatchFlowLogCodec.NAME), // See https://docs.aws.amazon.com/vpc/latest/userguide/flow-logs.html
        // TODO: Consider renaming this codec to not include the name CloudWatch, since the logs did not necessarily come from CloudWatch.
        UNKNOWN("Unknown log message", CloudWatchRawLogCodec.NAME);

        private String description;

        // The codec name, which is usually defined as a constant at the top of the codec class.
        private String codecName;

        Type(String description, String codecName) {

            this.description = description;
            this.codecName = codecName;
        }

        public String getDescription() {
            return description;
        }

        public String getCodecName() {
            return codecName;
        }

        public boolean isUnknown() {
            return this == UNKNOWN;
        }
    }
}
