package org.graylog.integrations.inputs;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.graylog.integrations.inputs.paloalto.PaloAltoCodec;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.journal.RawMessage;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PANCodecTest {

    // Structured syslog ??.
    final static String TRAFFIC_MESSAGE = "<14>1 2018-09-19T11:50:32-05:00 Panorama--2 - - - - 1,2018/09/19 11:50:32,007255000045717,TRAFFIC,end,2049,2018/09/19 11:50:32,10.20.30.40,10.20.30.40,10.20.30.40,10.20.30.40,HTTPS-strict,,,incomplete,vsys1,Public,Public,ethernet1/1,ethernet1/1,ALK Logging,2018/09/19 11:50:32,205742,1,64575,443,41304,443,0x400070,tcp,allow,412,272,140,6,2018/09/19 11:50:15,0,any,0,54196730,0x8000000000000000,10.20.30.40-10.20.30.40,10.20.30.40-10.20.30.40,0,4,2,tcp-fin,13,16,0,0,,Prod--2,from-policy,,,0,,0,,N/A,0,0,0,0";
    final static String THREAT_MESSAGE = "<14>1 2018-09-19T11:50:33-05:00 Panorama--1 - - - - 1,2018/09/19 11:50:33,007255000045716,THREAT,spyware,2049,2018/09/19 11:50:33,10.20.30.40,10.20.30.40,10.20.30.40,10.20.30.40,HTTPS-strict,,,ssl,vsys1,Public,Public,ethernet1/1,ethernet1/1,ALK Logging,2018/09/19 11:50:33,201360,1,21131,443,56756,443,0x80403000,tcp,alert,\"test.com/\",Suspicious TLS Evasion Found(14978),online_test.com,informational,client-to-server,1007133,0xa000000000000000,10.20.30.40-10.20.30.40,10.20.30.40-10.20.30.40,0,,1204440535977427988,,,0,,,,,,,,0,13,16,0,0,,Prod--1,,,,,0,,0,,N/A,spyware,AppThreat-8065-5006,0x0,0,4294967295";
                                        //<14>Aug 22 11:21:04 hq-lx-net-7.dart.org            1,2018/08/22 11:21:04,013201001141,THREAT,vulnerability,0,2018/08/22 11:21:02,10.0.190.116,10.0.2.225,0.0.0.0,0.0.0.0,DMZ-to-LAN_hq-direct-access,dart\abluitt,dart\kmendoza_admin,msrpc,vsys1,DMZ-2_L3,LAN_L3,ethernet1/3,ethernet1/6,Panorama,2018/08/22 11:21:02,398906,1,26475,135,0,0,0x2000,tcp,alert,"",Microsoft RPC Endpoint Mapper Detection(30845),any,informational,client-to-server,6585310726021616818,0x8000000000000000,10.0.0.0-10.255.255.255,10.0.0.0-10.255.255.255,0,,0,,,0,,,,,,,,0,346,12,0,0,,pa5220-hq-mdf-1,,,,,0,,0,,N/A,info-leak,AppThreat-8054-4933,0x0
    final static String SYSTEM_MESSAGE = "<14>1 2018-09-19T11:50:35-05:00 Panorama-1 - - - - 1,2018/09/19 11:50:35,000710000506,SYSTEM,general,0,2018/09/19 11:50:35,,general,,0,0,general,informational,\"Deviating device: Prod--2, Serial: 007255000045717, Object: N/A, Metric: mp-cpu, Value: 34\",1163103,0x0,0,0,0,0,,Panorama-1";

    @Test
    public void parseTest() {

        // Test System message results
        PaloAltoCodec codec = new PaloAltoCodec(null);
        Message message = codec.decode(new RawMessage(SYSTEM_MESSAGE.getBytes()));
        assertEquals("SYSTEM", message.getField("pa_type"));
        assertEquals(message.getField("module"), "general");
        assertEquals(message.getField("description"), "\"Deviating device: Prod--2");
        assertEquals(message.getField("serial_number"), "000710000506");
        assertEquals(message.getField("source"), "Panorama-1");
        assertEquals(message.getField("message"), "1,2018/09/19 11:50:35,000710000506,SYSTEM,general,0,2018/09/19 11:50:35,,general,,0,0,general,informational,\"Deviating device: Prod--2, Serial: 007255000045717, Object: N/A, Metric: mp-cpu, Value: 34\",1163103,0x0,0,0,0,0,,Panorama-1");
        assertEquals(message.getField("pa_severity"), "informational");
        assertEquals(message.getField("pa_time_generated"), "2018/09/19 11:50:35");
        assertEquals(message.getField("event_id"), "general");
        assertEquals(message.getField("pa_devicename"), "0");
        assertEquals(message.getField("content_type"), "general");
        assertEquals(message.getField("pa_virtualsys_name"), "0");
        assertEquals(0, ((DateTime) message.getField("timestamp")).compareTo(new DateTime("2018-09-19T11:50:35.000-05:00")));

        // Test Traffic message results
        message = codec.decode(new RawMessage(TRAFFIC_MESSAGE.getBytes()));
        assertEquals( message.getField("bytes_received"), 140L);
        assertEquals( message.getField("source"), "Panorama--2");
        assertEquals( message.getField("repeat_count"), 1L);
        assertEquals( message.getField("pa_time_received"), "2018/09/19 11:50:32");
        assertEquals( message.getField("interface_outbound"), "ethernet1/1");
        assertEquals( message.getField("packets"), 6L);
        assertEquals( message.getField("dest_location"), "10.20.30.40-10.20.30.40");
        assertEquals( message.getField("src_ip"), "10.20.30.40");
        assertEquals( message.getField("pa_time_generated"), "2018/09/19 11:50:32");
        assertEquals( message.getField("protocol"), "tcp");
        assertEquals( message.getField("content_type"), "end");
        assertEquals( message.getField("packets_sent"), 4L);
        assertEquals( message.getField("packets_received"), 2L);
        assertEquals( message.getField("action"), "allow");
        assertEquals( message.getField("pa_virtualsys_name"), "vsys1");
        assertEquals( message.getField("dest_port"), 443L);
        assertEquals( ((DateTime)message.getField("timestamp")).compareTo(new DateTime("2018-09-19T11:50:32.000-05:00")), 0);
        assertEquals( message.getField("rule_name"), "HTTPS-strict");
        assertEquals( message.getField("nat_src_ip"), "10.20.30.40");
        assertEquals( message.getField("session_id"), 205742L);
        assertEquals( message.getField("serial_number"), "007255000045717");
        assertEquals(message.getField("message"), "1,2018/09/19 11:50:32,007255000045717,TRAFFIC,end,2049,2018/09/19 11:50:32,10.20.30.40,10.20.30.40,10.20.30.40,10.20.30.40,HTTPS-strict,,,incomplete,vsys1,Public,Public,ethernet1/1,ethernet1/1,ALK Logging,2018/09/19 11:50:32,205742,1,64575,443,41304,443,0x400070,tcp,allow,412,272,140,6,2018/09/19 11:50:15,0,any,0,54196730,0x8000000000000000,10.20.30.40-10.20.30.40,10.20.30.40-10.20.30.40,0,4,2,tcp-fin,13,16,0,0,,Prod--2,from-policy,,,0,,0,,N/A,0,0,0,0");
        assertEquals( message.getField("bytes_sent"), 272L);
        assertEquals( message.getField("dst_zone"), "Public");
        assertEquals( message.getField("nat_src_port"), 41304L);
        assertEquals( message.getField("src_port"), 64575L);
        assertEquals( message.getField("src_location"), "10.20.30.40-10.20.30.40");
        assertEquals( message.getField("log_action"), "ALK Logging");
        assertEquals( message.getField("interface_inbound"), "ethernet1/1");
        assertEquals( message.getField("application"), "incomplete");
        assertEquals( message.getField("src_zone"), "Public");
        assertEquals( message.getField("bytes"), 412L);
        assertEquals( message.getField("dest_ip"), "10.20.30.40");
        assertEquals( message.getField("pa_type"), "TRAFFIC");
        assertEquals( message.getField("nat_dest_ip"), "10.20.30.40");
        assertEquals( message.getField("category"), "any");
        assertEquals( message.getField("nat_dest_port"), 443L);

        // TODO: Implement and test THREAT parsing.
        // message = codec.decode(new RawMessage(THREAT_MESSAGE.getBytes()));
        // assertEquals("THREAT", message.getField("pa_type"));
    }

    @Test
    public void invalidPositionTest() {

        // Verify that fields that have invalid positions (do not exist in the logs) are ignored.
        PaloAltoCodec codec = new PaloAltoCodec(null);

        // TODO: Inject custom configuration.
        Message message = codec.decode(new RawMessage(SYSTEM_MESSAGE.getBytes()));
        assertEquals("SYSTEM", message.getField("pa_type"));
    }

    /**
     * Helper for parsing PAN messages from HEX export
     */
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
                         .map(s -> s.substring(s.indexOf(">1") - 3, s.length()))
                         .collect(Collectors.toList());


        FileWriter writer = new FileWriter("capture-clean.txt");
        for (String str : hexVals) {
            writer.write(str + "\n");
        }
        writer.close();
    }

    private List<String> getTextLines() throws Exception {

        String s = new String(Files.readAllBytes(Paths.get("capture")));
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