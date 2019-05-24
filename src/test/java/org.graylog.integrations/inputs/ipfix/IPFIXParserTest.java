package org.graylog.integrations.inputs.ipfix;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class IPFIXParserTest {

    @Test
    @Ignore
    public void testPcapParsing() throws IOException {
        IPFIXParser ipfixParser = new IPFIXParser();
        ipfixParser.parsePcap("data/gelf.pcap");
    }
}
