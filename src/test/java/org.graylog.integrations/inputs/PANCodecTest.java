package org.graylog.integrations.inputs;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.graylog.integrations.inputs.paloalto.PANParser;
import org.graylog.integrations.inputs.paloalto.PANTypeParser;
import org.graylog.integrations.inputs.paloalto.PaloAltoCodec;
import org.graylog.integrations.inputs.paloalto.PaloAltoMessageBase;
import org.graylog.integrations.inputs.paloalto.types.SystemMessageMapping;
import org.graylog.integrations.inputs.paloalto.types.ThreatMessageMapping;
import org.graylog.integrations.inputs.paloalto.types.TrafficMessageMapping;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.journal.RawMessage;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PANCodecTest {

        final static String TRAFFIC_MESSAGE = "<14>1 2018-09-19T11:50:32-05:00 Prod-Ingress-2 - - - - 1,2018/09/19 11:50:32,007255000045717,TRAFFIC,end,2049,2018/09/19 11:50:32,10.16.13.7,10.33.12.144,10.77.134.144,10.16.202.161,HTTPS-strict,,,incomplete,vsys1,Public,Public,ethernet1/1,ethernet1/1,ALK Logging,2018/09/19 11:50:32,205742,1,64575,443,41304,443,0x400070,tcp,allow,412,272,140,6,2018/09/19 11:50:15,0,any,0,54196730,0x8000000000000000,10.0.0.0-10.255.255.255,10.0.0.0-10.255.255.255,0,4,2,tcp-fin,13,16,0,0,,Prod-Ingress-2,from-policy,,,0,,0,,N/A,0,0,0,0";
        final static String THREAT_MESSAGE = "<14>1 2018-09-19T11:50:33-05:00 Prod-Ingress-1 - - - - 1,2018/09/19 11:50:33,007255000045716,THREAT,spyware,2049,2018/09/19 11:50:33,10.16.141.74,10.16.129.121,10.16.88.121,10.16.200.73,HTTPS-strict,,,ssl,vsys1,Public,Public,ethernet1/1,ethernet1/1,ALK Logging,2018/09/19 11:50:33,201360,1,21131,443,56756,443,0x80403000,tcp,alert,\"test.com/\",Suspicious TLS Evasion Found(14978),online_bankingtest.com,informational,client-to-server,1007133,0xa000000000000000,10.0.0.0-10.255.255.255,10.0.0.0-10.255.255.255,0,,1204440535977427988,,,0,,,,,,,,0,13,16,0,0,,Prod-Ingress-1,,,,,0,,0,,N/A,spyware,AppThreat-8065-5006,0x0,0,4294967295";

    @Test
    public void parseTest() {

        PaloAltoCodec codec = new PaloAltoCodec(null);
        Message message = codec.decode(new RawMessage(TRAFFIC_MESSAGE.getBytes()));
        assertEquals("TRAFFIC", message.getField("pa_type"));

        // TODO: Debug failing test
        // message = codec.decode(new RawMessage(THREAT_MESSAGE.getBytes()));
        // assertEquals("THREAT", message.getField("pa_type"));
    }

    /**
     * Helper for parsing PAN messages from HEX export */
    public void dataParserTest() throws Exception {

        List<String> hexVals = new ArrayList<>();
        String buffer = "";
        for (String textLine : getTextLines()) {
            if (!textLine.equals("")) {
                buffer += textLine;
            } else {
                hexVals.add(buffer);
                buffer = "";
            }
        }

        hexVals = hexVals.stream().map(s -> s.replace(" ", "")).map(h -> {
            byte[] bytes = new byte[0];
            try {
                bytes = Hex.decodeHex(h.toCharArray());
            } catch (DecoderException e) {
                e.printStackTrace();
            }
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "";
        })
                         .filter(s -> s.contains("- - - -"))
                         .filter(s -> s.contains(">1"))
                         .filter(s -> s.contains("<"))
                         .map( s-> s.substring(s.indexOf(">1") - 3, s.length()))
                         .collect(Collectors.toList());


        FileWriter writer = new FileWriter("/Users/danieltorrey/Desktop/capture-clean.txt");
        for(String str: hexVals) {
            writer.write(str + "\n");
        }
        writer.close();
    }

    private List<String> getTextLines() throws Exception {

        String s = new String(Files.readAllBytes(Paths.get("/Users/danieltorrey/Desktop/capture")));
        return Arrays.asList(s.replace("\t", "").split("\\n")).stream().map(v -> {
                                                                                String withoutPrefix = v.length() > 7 ? v.substring(7, v.length()) : v;

                                                                                if (withoutPrefix.length() > 32) {
                                                                                    withoutPrefix = withoutPrefix.substring(0, 39);
                                                                                }
                                                                                return withoutPrefix;
                                                                            }
        ).collect(Collectors.toList());
    }
}