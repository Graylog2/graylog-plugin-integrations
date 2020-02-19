package org.graylog.integrations.okta;

import org.graylog2.plugin.inputs.codecs.Codec;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum OktaMessageType {

    OKTA_SYSTEM_LOG(Source.SYSTEM_LOG, "Okta System Log", OktaCodec.NAME,
                    OktaCodec.Factory.class, OktaTransport.NAME, OktaTransport.Factory.class),
    UNKNOWN();

    private Source source;
    private String label;
    private String codecName;
    private Class<? extends Codec.Factory> codecFactory;
    private String transportName;
    private Class<OktaTransport.Factory> transportFactory;

    OktaMessageType() {
    }

    OktaMessageType(Source source, String label, String codecName, Class<? extends Codec.Factory> codecFactory, String transportName, Class<OktaTransport.Factory> transportFactory) {
        this.source = source;
        this.label = label;
        this.codecName = codecName;
        this.codecFactory = codecFactory;
        this.transportName = transportName;
        this.transportFactory = transportFactory;
    }

    public Class<? extends Codec.Factory> getCodecFactory() {
        return codecFactory;
    }

    public Class<OktaTransport.Factory> getTransportFactory() {
        return transportFactory;
    }

    public String getLabel() {
        return label;
    }

    public String getCodecName() {
        return codecName;
    }

    public String getTransportName() {
        return transportName;
    }

    public enum Source {
        SYSTEM_LOG
    }

    /**
     * @return Return all message types except for UNKNOWN.
     */
    public static List<OktaMessageType> getMessageTypes() {

        return Arrays.stream(values()).filter(m -> !m.equals(UNKNOWN)).collect(Collectors.toList());
    }
}