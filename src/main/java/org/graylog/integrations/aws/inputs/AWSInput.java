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
package org.graylog.integrations.aws.inputs;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.assistedinject.Assisted;
import org.graylog.integrations.aws.codecs.AWSMetaCodec;
import org.graylog.integrations.aws.service.AWSService;
import org.graylog.integrations.aws.transports.KinesisTransport;
import org.graylog2.plugin.LocalMetricRegistry;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.buffers.InputBuffer;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.DropdownField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.MisfireException;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;

import javax.inject.Inject;

/**
 * General AWS input for all types of supported AWS logs.
 */
public class AWSInput extends MessageInput {

    public static final String NAME = "AWS";
    public static final String TYPE = "org.graylog.integrations.aws.inputs.AWSInput";

    private static final Logger LOG = LoggerFactory.getLogger(AWSInput.class);

    /**
     * Specifies one of the {@code AWSInputType} choices, which indicates which codec and transport
     * should be used.
     */
    public static final String CK_AWS_INPUT_TYPE = "aws_input_type";
    public static final String CK_TITLE = "title";
    public static final String CK_DESCRIPTION = "description";
    public static final String CK_GLOBAL = "global";
    public static final String CK_AWS_REGION = "aws_region";
    public static final String CK_ACCESS_KEY = "aws_access_key";
    public static final String CK_SECRET_KEY = "aws_secret_key";
    public static final String CK_ASSUME_ROLE_ARN = "aws_assume_role_arn";

    @Inject
    public AWSInput(@Assisted Configuration configuration,
                    MetricRegistry metricRegistry,
                    KinesisTransport.Factory transport,
                    LocalMetricRegistry localRegistry,
                    AWSMetaCodec.Factory codec,
                    Config config,
                    Descriptor descriptor,
                    ServerStatus serverStatus) {
        super(metricRegistry,
              configuration,
              transport.create(configuration),
              localRegistry,
              codec.create(configuration),
              config,
              descriptor,
              serverStatus);
    }

    @Override
    public void launch(InputBuffer buffer) throws MisfireException {
        super.launch(buffer);
    }

    @Override
    public void stop() {
        super.stop();
    }

    @FactoryClass
    public interface Factory extends MessageInput.Factory<AWSInput> {
        @Override
        AWSInput create(Configuration configuration);

        @Override
        Config getConfig();

        @Override
        Descriptor getDescriptor();
    }

    public static class Descriptor extends MessageInput.Descriptor {
        public Descriptor() {
            super(NAME, false, "");
        }
    }

    @ConfigClass
    public static class Config extends MessageInput.Config {

        @Inject
        public Config(KinesisTransport.Factory transport, AWSMetaCodec.Factory codec) {
            super(transport.getConfig(), codec.getConfig());
        }

        @Override
        public ConfigurationRequest combinedRequestedConfiguration() {
            ConfigurationRequest request = super.combinedRequestedConfiguration();

            // These config values will be shared amongst many AWS codecs and transports.
            request.addField(new DropdownField(
                    CK_AWS_REGION,
                    "AWS Region",
                    Region.US_EAST_1.id(),
                    AWSService.buildRegionChoices(),
                    "The AWS region the Kinesis stream is running in.",
                    ConfigurationField.Optional.NOT_OPTIONAL
            ));

            request.addField(new TextField(
                    CK_ACCESS_KEY,
                    "AWS access key",
                    "",
                    "Access key of an AWS user with sufficient permissions. (See documentation)",
                    ConfigurationField.Optional.OPTIONAL
            ));

            request.addField(new TextField(
                    CK_SECRET_KEY,
                    "AWS secret key",
                    "",
                    "Secret key of an AWS user with sufficient permissions. (See documentation)",
                    ConfigurationField.Optional.OPTIONAL,
                    TextField.Attribute.IS_PASSWORD
            ));

            request.addField(new TextField(
                    CK_ASSUME_ROLE_ARN,
                    "AWS assume role ARN",
                    "",
                    "Role ARN with required permissions (cross account access)",
                    ConfigurationField.Optional.OPTIONAL
            ));

            return request;
        }
    }
}
