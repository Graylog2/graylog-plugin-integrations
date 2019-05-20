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
package org.graylog.integrations.aws.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

// TODO: Do we really need a provider for a simple builder?
@Singleton
public class KinesisClientBuilderProvider implements Provider<KinesisClientBuilder> {

    @Inject
    public KinesisClientBuilderProvider() {
    }

    @Override
    public KinesisClientBuilder get() {
        return KinesisClient.builder();
    }
}
