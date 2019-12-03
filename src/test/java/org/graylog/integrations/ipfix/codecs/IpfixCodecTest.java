package org.graylog.integrations.ipfix.codecs;

import com.google.common.collect.ImmutableMap;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.journal.RawMessage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Ignore("All tests in this class are in development and not yet ready.")
public class IpfixCodecTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private IpfixCodec codec;
    private IpfixAggregator ipfixAggregator;

    @Before
    public void setUp() throws Exception {
        ipfixAggregator = new IpfixAggregator();
        codec = new IpfixCodec(Configuration.EMPTY_CONFIGURATION, ipfixAggregator);
    }

    @Ignore("Invalid CK_IPFIX_DEFINITION_PATH does not throw IOException, feature not ready.")
    @Test
    public void constructorFailsIfIPFixDefinitionsPathDoesNotExist() throws Exception {
        final File definitionsFile = temporaryFolder.newFile();
        assertThat(definitionsFile.delete()).isTrue();
        final ImmutableMap<String, Object> configMap = ImmutableMap.of(
                IpfixCodec.CK_IPFIX_DEFINITION_PATH, definitionsFile.getAbsolutePath());
        final Configuration configuration = new Configuration(configMap);
        assertThatExceptionOfType(IOException.class)
                .isThrownBy(() -> new IpfixCodec(configuration, ipfixAggregator))
                .withMessageEndingWith("(No such file or directory)");
    }

    @Ignore("Not ready.")
    @Test
    public void constructorSucceedsIfIPFixDefinitionsPathIsValidSONFile() throws Exception {

        final File tempFile = temporaryFolder.newFile("tempFile.json");
        // Load the custom definition file
        String custDefStr = "{ \"enterprise_number\": 3054, \"information_elements\": [ { \"element_id\": 110, " +
                            "\"name\": \"l7ApplicationId\", \"data_type\": \"unsigned32\" }, { \"element_id\": 111, " +
                            "\"name\": \"l7ApplicationName\", \"data_type\": \"string\" }, { \"element_id\": 120, " +
                            "\"name\": \"sourceIpCountryCode\", \"data_type\": \"string\" }, { \"element_id\": 121, " +
                            "\"name\": \"sourceIpCountryName\", \"data_type\": \"string\" }, { \"element_id\": 122, " +
                            "\"name\": \"sourceIpRegionCode\", \"data_type\": \"string\" }, { \"element_id\": 123, " +
                            "\"name\": \"sourceIpRegionName\", \"data_type\": \"string\" }, { \"element_id\": 125, " +
                            "\"name\": \"sourceIpCityName\", \"data_type\": \"string\" }, { \"element_id\": 126, " +
                            "\"name\": \"sourceIpLatitude\", \"data_type\": \"float32\" }, { \"element_id\": 127, " +
                            "\"name\": \"sourceIpLongitude\", \"data_type\": \"float32\" }, { \"element_id\": 140, " +
                            "\"name\": \"destinationIpCountryCode\", \"data_type\": \"string\" }, { \"element_id\": 141, " +
                            "\"name\": \"destinationIpCountryName\", \"data_type\": \"string\" }, { \"element_id\": 142, " +
                            "\"name\": \"destinationIpRegionCode\", \"data_type\": \"string\" }, { \"element_id\": 143, " +
                            "\"name\": \"destinationIpRegionName\", \"data_type\": \"string\" }, { \"element_id\": 145, " +
                            "\"name\": \"destinationIpCityName\", \"data_type\": \"string\" }, { \"element_id\": 146, " +
                            "\"name\": \"destinationIpLatitude\", \"data_type\": \"float32\" }, { \"element_id\": 147, " +
                            "\"name\": \"destinationIpLongitude\", \"data_type\": \"float32\" }, { \"element_id\": 160, " +
                            "\"name\": \"osDeviceId\", \"data_type\": \"unsigned8\" }, { \"element_id\": 161, " +
                            "\"name\": \"osDeviceName\", \"data_type\": \"string\" }, { \"element_id\": 162, " +
                            "\"name\": \"browserId\", \"data_type\": \"unsigned8\" }, { \"element_id\": 163, " +
                            "\"name\": \"browserName\", \"data_type\": \"string\" }, { \"element_id\": 176, " +
                            "\"name\": \"reverseOctetDeltaCount\", \"data_type\": \"unsigned64\" }, { \"element_id\": 177, " +
                            "\"name\": \"reversePacketDeltaCount\", \"data_type\": \"unsigned64\" }, { \"element_id\": 178, " +
                            "\"name\": \"sslConnectionEncryptionType\", \"data_type\": \"string\" }, { \"element_id\": 179, " +
                            "\"name\": \"sslEncryptionCipherName\", \"data_type\": \"string\" }, { \"element_id\": 180, " +
                            "\"name\": \"sslEncryptionKeyLength\", \"data_type\": \"unsigned16\" }, { \"element_id\": 182, " +
                            "\"name\": \"userAgent\", \"data_type\": \"string\" }, { \"element_id\": 183, " +
                            "\"name\": \"hostName\", \"data_type\": \"string\" }, { \"element_id\": 184, " +
                            "\"name\": \"uri\", \"data_type\": \"string\" }, { \"element_id\": 185, " +
                            "\"name\": \"dnsText\", \"data_type\": \"string\" }, { \"element_id\": 186, " +
                            "\"name\": \"sourceAsName\", \"data_type\": \"string\" }, { \"element_id\": 187, " +
                            "\"name\": \"destinationAsName\", \"data_type\": \"string\" }, { \"element_id\": 188, " +
                            "\"name\": \"transactionLatency\", \"data_type\": \"unsigned32\" }, { \"element_id\": 189, " +
                            "\"name\": \"dnsQueryHostName\", \"data_type\": \"string\" }, { \"element_id\": 190, " +
                            "\"name\": \"dnsResponseHostName\", \"data_type\": \"string\" }, { \"element_id\": 191, " +
                            "\"name\": \"dnsClasses\", \"data_type\": \"string\" }, { \"element_id\": 192, " +
                            "\"name\": \"threatType\", \"data_type\": \"string\" }, { \"element_id\": 193, " +
                            "\"name\": \"threatIpv4\", \"data_type\": \"ipv4address\" }, { \"element_id\": 194, " +
                            "\"name\": \"threatIpv6\", \"data_type\": \"ipv6address\" }, { \"element_id\": 195, " +
                            "\"name\": \"httpSession\", \"data_type\": \"subtemplatelist\" }, { \"element_id\": 196, " +
                            "\"name\": \"requestTime\", \"data_type\": \"unsigned32\" }, { \"element_id\": 197, " +
                            "\"name\": \"dnsRecord\", \"data_type\": \"subtemplatelist\" }, { \"element_id\": 198, " +
                            "\"name\": \"dnsName\", \"data_type\": \"string\" }, { \"element_id\": 199, " +
                            "\"name\": \"dnsIpv4Address\", \"data_type\": \"ipv4address\" }, { \"element_id\": 200, " +
                            "\"name\": \"dnsIpv6Address\", \"data_type\": \"ipv6address\" }, { \"element_id\": 201, " +
                            "\"name\": \"sni\", \"data_type\": \"string\" }, { \"element_id\": 457, " +
                            "\"name\": \"httpStatusCode\", \"data_type\": \"unsigned16\" }, { \"element_id\": 459, " +
                            "\"name\": \"httpRequestMethod\", \"data_type\": \"string\" }, { \"element_id\": 462, " +
                            "\"name\": \"httpMessageVersion\", \"data_type\": \"string\" } ] }\n";
        // Create a temporary json file.
        Files.write(tempFile.toPath(), custDefStr.getBytes(StandardCharsets.UTF_8));
        final ImmutableMap<String, Object> configMap = ImmutableMap.of(
                IpfixCodec.CK_IPFIX_DEFINITION_PATH, tempFile.getAbsolutePath());
        final Configuration configuration = new Configuration(configMap);
        IpfixCodec codec = new IpfixCodec(configuration, ipfixAggregator);
        final byte[] b = custDefStr.getBytes(StandardCharsets.UTF_8);
        final InetSocketAddress source = new InetSocketAddress(InetAddress.getLocalHost(), 12345);
        final RawMessage rawMessage = new RawMessage(b, source);
        final Collection<Message> messages = codec.decodeMessages(rawMessage);
        assertThat(messages).isNotNull();
    }
}