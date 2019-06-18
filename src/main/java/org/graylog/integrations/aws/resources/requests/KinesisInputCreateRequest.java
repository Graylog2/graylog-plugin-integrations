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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

/**
 * This request is used to save a new Kinesis AWS input.
 */
@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class KinesisInputCreateRequest {

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String AWS_INPUT_TYPE = "aws_input_type";
    private static final String AWS_ACCESS_KEY = "aws_access_key";
    private static final String AWS_SECRET_KEY = "aws_secret_key";
    private static final String STREAM_NAME = "stream_name";
    private static final String REGION = "region";
    private static final String BATCH_SIZE = "batch_size";
    private static final String ASSUME_ROLE_ARN = "assume_role_arn";
    private static final String GLOBAL = "global";
    private static final String ENABLE_THROTTLING = "enable_throttling";

    @JsonProperty(NAME)
    public abstract String name();

    @JsonProperty(DESCRIPTION)
    public abstract String description();

    @JsonProperty(AWS_INPUT_TYPE)
    public abstract String getAwsInputType();

    @JsonProperty(AWS_ACCESS_KEY)
    public abstract String awsAccessKey();

    @JsonProperty(AWS_SECRET_KEY)
    public abstract String awsSecretKey();

    @JsonProperty(STREAM_NAME)
    public abstract String streamName();

    @JsonProperty(ASSUME_ROLE_ARN)
    public abstract String assumeRoleARN();

    @JsonProperty(REGION)
    public abstract String region();

    @JsonProperty(BATCH_SIZE)
    public abstract int batchSize();

    @JsonProperty(GLOBAL)
    public abstract boolean global();

    @JsonProperty(ENABLE_THROTTLING)
    public abstract boolean enableThrottling();

    @JsonCreator
    public static KinesisInputCreateRequest create(@JsonProperty(NAME) String name,
                                                   @JsonProperty(DESCRIPTION) String description,
                                                   @JsonProperty(AWS_INPUT_TYPE) String awsInputType,
                                                   @JsonProperty(AWS_ACCESS_KEY) String awsAccessKey,
                                                   @JsonProperty(AWS_SECRET_KEY) String awsSecretKey,
                                                   @JsonProperty(STREAM_NAME) String streamName,
                                                   @JsonProperty(REGION) String region,
                                                   @JsonProperty(BATCH_SIZE) int batchSize,
                                                   @JsonProperty(ASSUME_ROLE_ARN) String assumeRoleArn,
                                                   @JsonProperty(GLOBAL) boolean global,
                                                   @JsonProperty(ENABLE_THROTTLING) boolean enableThrottling) {
        return new AutoValue_KinesisInputCreateRequest(name, description, awsInputType, awsAccessKey, awsSecretKey, streamName, assumeRoleArn, region, batchSize, global, enableThrottling);
    }
}