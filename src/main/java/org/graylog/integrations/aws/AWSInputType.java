package org.graylog.integrations.aws;

import org.graylog.integrations.aws.codecs.CloudWatchRawLogCodec;
import org.graylog.integrations.aws.transports.KinesisTransport;
import org.graylog2.plugin.inputs.codecs.AbstractCodec;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.graylog2.plugin.inputs.transports.Transport;

import java.util.Arrays;

/**
 * Identifies the type of transport and codec that each type of AWS logs will use.
 */
public enum AWSInputType {

    // Raw string stored in Kinesis.
    KINESIS_RAW(AbstractCodec.Factory.class, KinesisTransport.Factory.class),

    // Raw string originating in CloudWatch.
    KINESIS_CLOUD_WATCH_RAW(CloudWatchRawLogCodec.Factory.class, KinesisTransport.Factory.class),

    // Flow log originating from CloudWatch.
    KINESIS_CLOUD_WATCH_FLOW_LOGS(CloudWatchRawLogCodec.Factory.class, KinesisTransport.Factory.class);

    private Class<? extends Codec.Factory> codecFactory;
    private Class<? extends Transport.Factory> transportFactory;

    AWSInputType(Class<? extends Codec.Factory> codecFactory, Class<? extends Transport.Factory> transportFactory) {
        this.codecFactory = codecFactory;
        this.transportFactory = transportFactory;
    }

    public Class<? extends Codec.Factory> getCodecFactory() {
        return codecFactory;
    }

    public Class<? extends Transport.Factory> getTransportFactory() {
        return transportFactory;
    }

    /**
     * @return true if the type uses the KinesisTransport.
     */
    public boolean isKinesis() {

        return Arrays.asList(KINESIS_RAW,
                             KINESIS_CLOUD_WATCH_RAW,
                             KINESIS_CLOUD_WATCH_FLOW_LOGS).contains(this);
    }
}