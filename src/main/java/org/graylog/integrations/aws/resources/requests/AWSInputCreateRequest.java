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
package org.graylog.integrations.aws.resources.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import javax.annotation.Nullable;

/**
 * This request is used to save a new Kinesis AWS input. Each type of AWS input will use it's own request
 * object due to typically very unique required fields for each.
 */
@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class AWSInputCreateRequest implements AWSRequest {

    private static final String NAME = "name";
    private static final String AWS_MESSAGE_TYPE = "aws_input_type";
    private static final String STREAM_NAME = "stream_name";
    private static final String BATCH_SIZE = "batch_size";
    private static final String GLOBAL = "global";
    private static final String THROTTLING_ALLOWED = "enable_throttling";
    private static final String ADD_FLOW_LOG_PREFIX = "add_flow_log_prefix";

    @JsonProperty(REGION)
    public abstract String region();

    @JsonProperty(AWS_ACCESS_KEY_ID)
    public abstract String awsAccessKeyId();

    @JsonProperty(AWS_SECRET_ACCESS_KEY)
    public abstract String awsSecretAccessKey();

    @Nullable
    @JsonProperty(ASSUME_ROLE_ARN)
    public abstract String assumeRoleArn();

    @JsonProperty(NAME)
    public abstract String name();

    @JsonProperty(AWS_MESSAGE_TYPE)
    public abstract String awsMessageType();

    @JsonProperty(STREAM_NAME)
    public abstract String streamName();

    @JsonProperty(BATCH_SIZE)
    public abstract int batchSize();

    @JsonProperty(GLOBAL)
    public abstract boolean global();

    @JsonProperty(THROTTLING_ALLOWED)
    public abstract boolean throttlingAllowed();

    @JsonProperty(ADD_FLOW_LOG_PREFIX)
    public abstract boolean addFlowLogPrefix();

    @JsonCreator
    public static AWSInputCreateRequest create(@JsonProperty(REGION) String region,
                                               @JsonProperty(AWS_ACCESS_KEY_ID) String awsAccessKey,
                                               @JsonProperty(AWS_SECRET_ACCESS_KEY) String awsSecretKey,
                                               @JsonProperty(ASSUME_ROLE_ARN) String assumeRoleArn,
                                               @JsonProperty(NAME) String name,
                                               @JsonProperty(AWS_MESSAGE_TYPE) String awsMessageType,
                                               @JsonProperty(STREAM_NAME) String streamName,
                                               @JsonProperty(BATCH_SIZE) int batchSize,
                                               @JsonProperty(GLOBAL) boolean global,
                                               @JsonProperty(THROTTLING_ALLOWED) boolean enableThrottling,
                                               @JsonProperty(ADD_FLOW_LOG_PREFIX) boolean addFlowLogPrefix) {
        return new AutoValue_AWSInputCreateRequest(region, awsAccessKey, awsSecretKey, assumeRoleArn,
                                                   name, awsMessageType, streamName, batchSize, global,
                                                   enableThrottling, addFlowLogPrefix);
    }
}