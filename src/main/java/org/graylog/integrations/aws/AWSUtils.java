package org.graylog.integrations.aws;

/**
 * A common utils class for the AWS integrations.
 */
public class AWSUtils {

    public static final String SOURCE_GROUP_IDENTIFIER = "aws_source";
    public static final String FIELD_LOG_GROUP = "aws_log_group";
    public static final String FIELD_LOG_STREAM = "aws_log_stream";

    // This is a non-instantiable utils class.
    private AWSUtils() {
    }
}
