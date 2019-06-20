package org.graylog.integrations.aws;

import com.google.common.collect.Maps;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.RegionMetadata;

import java.util.Map;

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
