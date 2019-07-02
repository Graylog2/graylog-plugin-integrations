package org.graylog.integrations.aws;

import org.graylog.integrations.aws.codecs.KinesisCloudWatchFlowLogCodec;
import org.graylog.integrations.aws.codecs.KinesisRawLogCodec;
import org.graylog.integrations.aws.transports.KinesisTransport;
import org.graylog2.plugin.inputs.codecs.AbstractCodec;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.graylog2.plugin.inputs.transports.Transport;

/**
 * Identifies the type of input for a particular log source (eg. Cloud Watch or Kinesis) and
 * log format.
 *
 * This type will be saved with the input to indicate which transport and codec should be used.
 */
public enum AWSMessageType {

    /**
     * A raw string stored in CloudWatch or Kinesis.
     */
    KINESIS_RAW(Source.KINESIS, "Raw", KinesisRawLogCodec.NAME,
                AbstractCodec.Factory.class, KinesisTransport.Factory.class),

    // Flow Logs delivered to Kinesis via CloudWatch subscriptions.
    KINESIS_FLOW_LOGS(Source.KINESIS, "Flow Log", KinesisCloudWatchFlowLogCodec.NAME,
                      KinesisCloudWatchFlowLogCodec.Factory.class, KinesisTransport.Factory.class),

    UNKNOWN();

    private Source source;
    private String label;
    private String codecName;
    private Class<? extends Codec.Factory> codecFactory;
    private Class<? extends Transport.Factory> transportFactory;

    AWSMessageType() {
    }

    AWSMessageType(Source source, String label, String codecName, Class<? extends Codec.Factory> codecFactory, Class<? extends Transport.Factory> transportFactory) {
        this.source = source;
        this.label = label;
        this.codecName = codecName;
        this.codecFactory = codecFactory;
        this.transportFactory = transportFactory;
    }

    public Class<? extends Codec.Factory> getCodecFactory() {
        return codecFactory;
    }

    public Class<? extends Transport.Factory> getTransportFactory() {
        return transportFactory;
    }

    public String getLabel() {
        return label;
    }

    public String getCodecName() {
        return codecName;
    }

    /**
     * @return True if the {@link AWSMessageType} enum instance has a Kinesis source.
     */
    public boolean isKinesis() {
        return Source.KINESIS.equals(this.source);
    }

    /**
     * Each source will require its own specific set of input configuration fields. Multiple {@link AWSMessageType}
     * enum instances might correspond to the same source and thus require the same configuration fields.
     * For example, {@code AWSMessageType.KINESIS_RAW} and {@code AWSMessageType.KINESIS_FLOW_LOGS}
     * correspond to two different types of AWS messages, but share a common KINESIS source.
     */
    public enum Source {
        KINESIS
    }
}