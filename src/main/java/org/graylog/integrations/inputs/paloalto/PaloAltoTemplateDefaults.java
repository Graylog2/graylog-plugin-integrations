/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.integrations.inputs.paloalto;

/**
 * Default PAN message templates. Used for tests and initial configurations on input creation.
 *
 * These should be maintained to always reflect the latest PAN message versions.
 *
 * These are for version 8.1.
 *
 * TODO: Consider moving this JSON to static resource fields.
 */
public class PaloAltoTemplateDefaults {

    public static String POSITION = "position";
    public static String FIELD = "field";
    public static  String TYPE = "type";

    private PaloAltoTemplateDefaults() {

    }

    /**
     * GIM rename mappings (from Palo Alto docs to Graylog field names):
     * source -> src
     * address -> addr
     * destination -> dst
     */

    public static String SYSTEM_TEMPLATE = headerRow() +
                                           "1,future_use1,STRING\n" +
                                           "2,receive_time,STRING\n" +
                                           "3,serial_number,STRING\n" +
                                           "4,type,STRING\n" +
                                           "5,content_threat_type,STRING\n" +
                                           "6,future_use2,STRING\n" +
                                           "7,generated_time,STRING\n" +
                                           "8,virtual_system,STRING\n" +
                                           "9,event_id,STRING\n" +
                                           "10,object,STRING\n" +
                                           "11,future_use3,STRING\n" +
                                           "12,future_use4,STRING\n" +
                                           "13,module,STRING\n" +
                                           "14,severity,STRING\n" +
                                           "15,description,STRING\n" +
                                           "16,sequence_number,STRING\n" +
                                           "17,action_flags,STRING\n" +
                                           "18,device_group_hierarchy_l1,STRING\n" +
                                           "19,device_group_hierarchy_l2,STRING\n" +
                                           "20,device_group_hierarchy_l3,STRING\n" +
                                           "21,device_group_hierarchy_l4,STRING\n" +
                                           "22,virtual_system_name,STRING\n" +
                                           "23,device_name,STRING";

    private static String headerRow() {
        return POSITION + "," + FIELD + "," + TYPE + "\n";
    }

    public static String THREAT_TEMPLATE = headerRow() +
                                           "1,future_use1,STRING\n" + 
                                           "2,receive_time,STRING\n" +
                                           "3,serial_number,STRING\n" +
                                           "4,type,STRING\n" +
                                           "5,threat_content_type,STRING\n" +
                                           "6,future_use2,STRING\n" +
                                           "7,generated_time,STRING\n" +
                                           "8,src_addr,STRING\n" +
                                           "9,dst_addr,STRING\n" +
                                           "10,nat_src_addr,STRING\n" +
                                           "11,nat_dst_addr,STRING\n" +
                                           "12,rule_name,STRING\n" +
                                           "13,src_user,STRING\n" +
                                           "14,dst_user,STRING\n" +
                                           "15,application,STRING\n" +
                                           "16,virtual_system,STRING\n" +
                                           "17,src_zone,STRING\n" +
                                           "18,dst_zone,STRING\n" +
                                           "19,inbound_interface,STRING\n" +
                                           "20,outbound_interface,STRING\n" +
                                           "21,log_action,STRING\n" +
                                           "22,future_use3,STRING\n" +
                                           "23,session_id,LONG\n" +
                                           "24,repeat_count,LONG\n" +
                                           "25,src_port,LONG\n" +
                                           "26,dst_port,LONG\n" +
                                           "27,nat_src_port,LONG\n" +
                                           "28,nat_dst_port,LONG\n" +
                                           "29,flags,STRING\n" +
                                           "30,protocol,STRING\n" +
                                           "31,action,STRING\n" +
                                           "32,miscellaneous,STRING\n" +
                                           "33,threat_id,STRING\n" +
                                           "34,category,STRING\n" +
                                           "35,severity,STRING\n" +
                                           "36,direction,STRING\n" +
                                           "37,sequence_number,STRING\n" +
                                           "38,action_flags,STRING\n" +
                                           "39,src_location,STRING\n" +
                                           "40,dst_location,STRING\n" +
                                           "41,future_use4,STRING\n" +
                                           "42,content_type,STRING\n" +
                                           "43,pcap_id,STRING\n" +
                                           "44,file_digest,STRING\n" +
                                           "45,cloud,STRING\n" +
                                           "46,url_index,LONG\n" +
                                           "47,user_agent,STRING\n" +
                                           "48,file_type,STRING\n" +
                                           "49,x-forwarded-for,STRING\n" +
                                           "50,referer,STRING\n" +
                                           "51,sender,STRING\n" +
                                           "52,subject,STRING\n" +
                                           "53,recipient,STRING\n" +
                                           "54,report_id,LONG\n" +
                                           "55,device_group_hierarchy_l1,LONG\n" +
                                           "56,device_group_hierarchy_l2,LONG\n" +
                                           "57,device_group_hierarchy_l3,LONG\n" +
                                           "58,device_group_hierarchy_l4,LONG\n" +
                                           "59,virtual_system_name,STRING\n" +
                                           "60,device_name,STRING\n" +
                                           "61,future_use5,STRING\n" +
                                           "62,src_vm_uuid,STRING\n" +
                                           "63,dst_vm_uuid,STRING\n" +
                                           "64,http_method,STRING\n" +
                                           "65,tunnel_id_imsi,STRING\n" +
                                           "66,monitor_tag_imei,STRING\n" +
                                           "67,parent_session_id,STRING\n" +
                                           "68,parent_start_time,STRING\n" +
                                           "69,tunnel_type,STRING\n" +
                                           "70,threat_category,STRING\n" +
                                           "71,content_version,STRING\n" +
                                           "72,future_use6,STRING\n" +
                                           "73,sctp_association_id,LONG\n" +
                                           "74,payload_protocol_id,LONG\n" +
                                           "75,http_headers,STRING\n" +
                                           "76,url_category_list,STRING\n" +
                                           "77,rule_uuid,long\n" +
                                           "77,http2_connection,STRING";

    public static String TRAFFIC_TEMPLATE = headerRow() +
                                            "1,future_use1,STRING\n" +
                                            "2,receive_time,STRING\n" +
                                            "3,serial_number,long\n" +
                                            "4,type,STRING\n" +
                                            "5,threat_content_type,STRING\n" +
                                            "6,future_use2,STRING\n" +
                                            "7,generated_time,STRING\n" +
                                            "8,src_addr,STRING\n" +
                                            "9,dst_addr,STRING\n" +
                                            "10,nat_src_addr,STRING\n" +
                                            "11,nat_dst_addr,STRING\n" +
                                            "12,rule_name,STRING\n" +
                                            "13,src_user,STRING\n" +
                                            "14,dst_user,STRING\n" +
                                            "15,application,STRING\n" +
                                            "16,virtual_system,STRING\n" +
                                            "17,src_zone,STRING\n" +
                                            "18,dst_zone,STRING\n" +
                                            "19,inbound_interface,STRING\n" +
                                            "20,outbound_interface,STRING\n" +
                                            "21,log_action,STRING\n" +
                                            "22,future_use3,STRING\n" +
                                            "23,session_id,LONG\n" +
                                            "24,repeat_count,LONG\n" +
                                            "25,src_port,LONG\n" +
                                            "26,dst_port,LONG\n" +
                                            "27,nat_src_port,LONG\n" +
                                            "28,nat_dst_port,LONG\n" +
                                            "29,flags,STRING\n" +
                                            "30,protocol,STRING\n" +
                                            "31,action,STRING\n" +
                                            "32,bytes,LONG\n" +
                                            "33,bytes_sent,LONG\n" +
                                            "34,bytes_received,LONG\n" +
                                            "35,packets,LONG\n" +
                                            "36,start_time,STRING\n" +
                                            "37,elapsed_time,STRING\n" +
                                            "38,category,STRING\n" +
                                            "39,future_use4,STRING\n" +
                                            "40,sequence_number,STRING\n" +
                                            "41,action_flags,STRING\n" +
                                            "42,src_location,STRING\n" +
                                            "43,dst_location,STRING\n" +
                                            "44,future_use5,STRING\n" +
                                            "45,packets_sent,LONG\n" +
                                            "46,packets_received,LONG\n" +
                                            "47,session_end_reason,STRING\n" +
                                            "48,device_group_hierarchy_l1,STRING\n" +
                                            "49,device_group_hierarchy_l2,STRING\n" +
                                            "50,device_group_hierarchy_l3,STRING\n" +
                                            "51,device_group_hierarchy_l4,STRING\n" +
                                            "52,virtual_system_name,STRING\n" +
                                            "53,device_name,STRING\n" +
                                            "54,action_src,STRING\n" +
                                            "55,src_vm_uuid,STRING\n" +
                                            "56,dst_vm_uuid,STRING\n" +
                                            "57,tunnel_id_imsi,STRING\n" +
                                            "58,monitor_tag_imei,STRING\n" +
                                            "59,parent_session_id,STRING\n" +
                                            "60,parent_start_time,STRING\n" +
                                            "61,tunnel_type,STRING\n" +
                                            "62,sctp_association_id,STRING\n" +
                                            "63,sctp_chunks,STRING\n" +
                                            "64,sctp_chunks_sent,STRING\n" +
                                            "65,sctp_chunks_received,STRING\n" +
                                            "66,rule_uuid,long\n" +
                                            "67,http2_connection,STRING";
}