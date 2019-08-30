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
package org.graylog.integrations;

import org.graylog.integrations.audit.IntegrationsAuditEventTypes;
import org.graylog.integrations.aws.AWSPermissions;
import org.graylog.integrations.aws.codecs.AWSCodec;
import org.graylog.integrations.aws.codecs.KinesisCloudWatchFlowLogCodec;
import org.graylog.integrations.aws.codecs.KinesisRawLogCodec;
import org.graylog.integrations.aws.inputs.AWSInput;
import org.graylog.integrations.aws.resources.AWSResource;
import org.graylog.integrations.aws.resources.KinesisSetupResource;
import org.graylog.integrations.aws.transports.AWSTransport;
import org.graylog.integrations.aws.transports.KinesisTransport;
import org.graylog.integrations.inputs.paloalto.PaloAltoCodec;
import org.graylog.integrations.inputs.paloalto.PaloAltoTCPInput;
import org.graylog2.plugin.PluginConfigBean;
import org.graylog2.plugin.PluginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.IamClientBuilder;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;

import java.util.Collections;
import java.util.Set;

/**
 * Extend the PluginModule abstract class here to add you plugin to the system.
 */
public class IntegrationsModule extends PluginModule {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationsModule.class);

    /**
     * Returns all configuration beans required by this plugin.
     *
     * Implementing this method is optional. The default method returns an empty {@link Set}.
     */
    @Override
    public Set<? extends PluginConfigBean> getConfigBeans() {
        return Collections.emptySet();
    }

    @Override
    protected void configure() {
        /*
         * Register your plugin types here.
         *
         * Examples:
         *
         * addMessageInput(Class<? extends MessageInput>);
         * addMessageFilter(Class<? extends MessageFilter>);
         * addMessageOutput(Class<? extends MessageOutput>);
         * addPeriodical(Class<? extends Periodical>);
         * addAlarmCallback(Class<? extends AlarmCallback>);
         * addInitializer(Class<? extends Service>);
         * addRestResource(Class<? extends PluginRestResource>);
         *
         *
         * Add all configuration beans returned by getConfigBeans():
         *
         * addConfigBeans();
         */

        addAuditEventTypes(IntegrationsAuditEventTypes.class);

        // Palo Alto Networks
        LOG.debug("Registering message input: {}", PaloAltoTCPInput.NAME);
        addMessageInput(PaloAltoTCPInput.class);
        addCodec(PaloAltoCodec.NAME, PaloAltoCodec.class);

        // AWS
        addCodec(AWSCodec.NAME, AWSCodec.class);
        addCodec(KinesisCloudWatchFlowLogCodec.NAME, KinesisCloudWatchFlowLogCodec.class);
        addCodec(KinesisRawLogCodec.NAME, KinesisRawLogCodec.class);
        addMessageInput(AWSInput.class);
        addPermissions(AWSPermissions.class);
        addRestResource(AWSResource.class);
        addRestResource(KinesisSetupResource.class);
        addTransport(AWSTransport.NAME, AWSTransport.class);
        addTransport(KinesisTransport.NAME, KinesisTransport.class);
        bind(IamClientBuilder.class).toProvider(IamClient::builder);
        bind(CloudWatchLogsClientBuilder.class).toProvider(CloudWatchLogsClient::builder);
        bind(KinesisClientBuilder.class).toProvider(KinesisClient::builder);
    }
}