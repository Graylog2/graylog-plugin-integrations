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
package org.graylog.integrations.aws.cloudwatch;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog.autovalue.WithBeanGetter;

import java.util.List;

/**
 * A collection of CloudWatch log events that was generated by a Kinesis CloudWatch log subscription.
 * See for more info: https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/Subscriptions.html
 *
 * <p/>
 * Example payload:
 * <pre>
 * {
 *   "messageType": "DATA_MESSAGE",
 *   "owner": "123456789",
 *   "logGroup": "aws-plugin-test-flows",
 *   "logStream": "eni-aaaaaaaa-all",
 *   "subscriptionFilters": ["match-all"],
 *   "logEvents": [
 *     {
 *       "id": "33503748002479370955346306650196094071913271643270021120",
 *       "timestamp": 1502360020000,
 *       "message": "2 123456789 eni-aaaaaaaa 10.0.27.226 10.42.96.199 3604 17720 17 1 132 1502360020 1502360079 REJECT OK"
 *     },
 *     {
 *       "id": "33503748002479370955346306650196094071913271643270021127",
 *       "timestamp": 1502360020000,
 *       "message": "2 123456789 eni-aaaaaaaa 10.0.34.113 10.42.96.199 53421 17720 6 1 48 1502360020 1502360079 REJECT OK"
 *     }
 *   ]
 * }
 * </pre>
 */

@JsonAutoDetect
@AutoValue
@WithBeanGetter
public abstract class CloudWatchLogSubscriptionData {

    private static final String MESSAGE_TYPE = "messageType";
    private static final String OWNER = "owner";
    private static final String LOG_GROUP = "logGroup";
    private static final String LOG_STREAM = "logStream";
    private static final String SUBSCRIPTION_FILTERS = "subscriptionFilters";
    private static final String LOG_EVENTS = "logEvents";

    @JsonProperty(MESSAGE_TYPE)
    public abstract String messageType();

    @JsonProperty(OWNER)
    public abstract String owner();

    @JsonProperty(LOG_GROUP)
    public abstract String logGroup();

    @JsonProperty(LOG_STREAM)
    public abstract String logStream();

    @JsonProperty(SUBSCRIPTION_FILTERS)
    public abstract List<String> subscriptionFilters();

    @JsonProperty(LOG_EVENTS)
    public abstract List<CloudWatchLogEvent> logEvents();

    @JsonCreator
    public static CloudWatchLogSubscriptionData create(@JsonProperty(MESSAGE_TYPE) String messageType,
                                                       @JsonProperty(OWNER) String owner,
                                                       @JsonProperty(LOG_GROUP) String logGroup,
                                                       @JsonProperty(LOG_STREAM) String logStream,
                                                       @JsonProperty(SUBSCRIPTION_FILTERS) List<String> subscriptionFilters,
                                                       @JsonProperty(LOG_EVENTS) List<CloudWatchLogEvent> logEvents) {
        return new AutoValue_CloudWatchLogSubscriptionData(messageType, owner, logGroup, logStream, subscriptionFilters, logEvents);
    }


}
