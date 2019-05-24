package org.graylog.integrations.inputs.ipfix;

import io.kaitai.struct.ByteBufferKaitaiStream;
import org.graylog.plugins.Pcap;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * A parser to test out Kaitai declarative struct parsing.
 */
public class IPFIXParser {

    /**
     * Prove out parsing of a PCAP to start with.
     *
     * @param fileName
     */
    public Pcap parsePcap(String fileName) throws IOException {

        String absolutePcapPath = getFileFromResources(fileName).getAbsoluteFile().toString();

        //
        return new Pcap(new ByteBufferKaitaiStream(absolutePcapPath));
    }

    /**
     * Read file from resources.
     */
    private File getFileFromResources(String fileName) {

        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        }

        return new File(resource.getFile());
    }
}
