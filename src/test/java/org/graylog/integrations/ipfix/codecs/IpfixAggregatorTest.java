package org.graylog.integrations.ipfix.codecs;

import com.google.common.io.Resources;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.pkts.Pcap;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;
import org.graylog.integrations.ipfix.InformationElementDefinitions;
import org.graylog.integrations.ipfix.Utils;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.codecs.CodecAggregator;
import org.graylog2.plugin.journal.RawMessage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class IpfixAggregatorTest {
    private static final Logger LOG = LoggerFactory.getLogger(IpfixAggregatorTest.class);
    private final InetSocketAddress someAddress = InetSocketAddress.createUnresolved("192.168.1.1", 999);
    private InformationElementDefinitions definitions = new InformationElementDefinitions(
            Resources.getResource("ipfix-iana-elements.json"),
            Resources.getResource("ixia-ipfix.json")
    );

    @Test
    public void completePacket() throws IOException {
        final ByteBuf packetBytes = Utils.readPacket("templates-data.ipfix");

        final IpfixAggregator aggregator = new IpfixAggregator();
        final CodecAggregator.Result result = aggregator.addChunk(packetBytes, someAddress);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isNotNull();
        // TODO complete unit test
    }

    @Test
    public void multipleMessagesTemplateLater() throws IOException {
        final ByteBuf datasetOnlyBytes = Utils.readPacket("dataset-only.ipfix");
        final ByteBuf withTemplatesBytes = Utils.readPacket("templates-data.ipfix");
        final IpfixAggregator ipfixAggregator = new IpfixAggregator();
        final CodecAggregator.Result resultQueued = ipfixAggregator.addChunk(datasetOnlyBytes, someAddress);
        assertThat(resultQueued.isValid()).isTrue();
        assertThat(resultQueued.getMessage()).isNull();

        final CodecAggregator.Result resultComplete = ipfixAggregator.addChunk(withTemplatesBytes, someAddress);
        assertThat(resultComplete.isValid()).isTrue();
        assertThat(resultComplete.getMessage()).isNotNull();
        // TODO complete unit test

    }

    @Test
    public void dataAndDataTemplate() throws IOException {

        final IpfixAggregator ipfixAggregator = new IpfixAggregator();
        final IpfixCodec codec = new IpfixCodec(Configuration.EMPTY_CONFIGURATION, ipfixAggregator);

        AtomicInteger messageCount = new AtomicInteger();
        try (InputStream stream = Resources.getResource("data-datatemplate.pcap").openStream()) {
            final Pcap pcap = Pcap.openStream(stream);
            pcap.loop(packet -> {
                if (packet.hasProtocol(Protocol.UDP)) {
                    final UDPPacket udp = (UDPPacket) packet.getPacket(Protocol.UDP);
                    final InetSocketAddress source = new InetSocketAddress(udp.getParentPacket().getSourceIP(), udp.getSourcePort());
                    byte[] payload = new byte[udp.getPayload().getReadableBytes()];
                    udp.getPayload().getBytes(payload);
                    final ByteBuf buf = Unpooled.wrappedBuffer(payload);
                    final CodecAggregator.Result result = ipfixAggregator.addChunk(buf, source);
                    final ByteBuf ipfixRawBuf = result.getMessage();
                    if (ipfixRawBuf != null) {
                        byte[] bytes = new byte[ipfixRawBuf.readableBytes()];
                        ipfixRawBuf.getBytes(0, bytes);
                        final Collection<Message> messages = codec.decodeMessages(new RawMessage(bytes));
                        if (messages != null) {
                            messageCount.addAndGet(messages.size());
                        }
                    }
                }
                return true;
            });
        } catch (IOException e) {
            LOG.error("Cannot process PCAP stream", e);
        }

        assertThat(messageCount.get()).isEqualTo(4L);
    }
}