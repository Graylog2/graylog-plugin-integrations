package org.graylog.integrations.inputs;

import org.graylog.integrations.inputs.paloalto.PANParser;
import org.graylog.integrations.inputs.paloalto.PANTypeParser;
import org.graylog.integrations.inputs.paloalto.PaloAltoMessageBase;
import org.graylog.integrations.inputs.paloalto.types.ThreatMessageMapping;
import org.junit.Test;

public class PaloAltoNetworksInputTest {

    @Test
    public void parseTest() {

        PANParser parser = new PANParser();
        PaloAltoMessageBase p = parser.parse("Sep 13 08:17:33 1,2011/09/13 08: 17:33,0004C101953,TRAFFIC,start,1,2011/09/13 08:17:27,10.20.1.12,10.10.11.42,0.0.0.0,0.0.0.0,rule_for_logs,,,ping,vsys1,trust,untrust,ethernet1/1,ethernet1/2,algosyslog,2011/09/13 08:17:32,116923,6,0,0,0,0,0x0,icmp,allow,588,588,588,6,2011/09/13 08:17:27,0,any,0");

        PANTypeParser PARSER_THREAT = new PANTypeParser(new ThreatMessageMapping());
        PARSER_THREAT.parseFields(p.fields());
    }
}