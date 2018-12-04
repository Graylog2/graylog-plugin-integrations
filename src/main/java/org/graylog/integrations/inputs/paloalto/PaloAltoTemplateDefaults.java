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

    public static String SYSTEM_TEMPLATE = headerRow() +
                                           "1,receive_time,STRING\n" +
                                           "2,serial_number,STRING\n" +
                                           "3,type,STRING\n" +
                                           "4,content_threat_type,STRING\n" +
                                           "5,future_use1,STRING\n" +
                                           "6,generated_time,STRING\n" +
                                           "7,virtual_system,STRING\n" +
                                           "8,event_id,STRING\n" +
                                           "9,object,STRING\n" +
                                           "10,future_use2,STRING\n" +
                                           "11,future_use3,STRING\n" +
                                           "12,module,STRING\n" +
                                           "13,severity,STRING\n" +
                                           "14,description,STRING\n" +
                                           "15,sequence_number,STRING\n" +
                                           "16,action_flags,STRING\n" +
                                           "17,device_group_hierarchy_l1,STRING\n" +
                                           "18,device_group_hierarchy_l2,STRING\n" +
                                           "19,device_group_hierarchy_l3,STRING\n" +
                                           "20,device_group_hierarchy_l4,STRING\n" +
                                           "21,virtual_system_name,STRING\n" +
                                           "22,device_name,STRING";

    private static String headerRow() {
        return POSITION + "," + FIELD + "," + TYPE + "\n";
    }

    public static String THREAT_TEMPLATE = headerRow() +
                                           "1,receive_time,STRING\n" +
                                           "2,serial_number,STRING\n" +
                                           "3,type,STRING\n" +
                                           "4,threat_content_type,STRING\n" +
                                           "5,future_use1,STRING\n" +
                                           "6,generated_time,STRING\n" +
                                           "7,src_ip,STRING\n" +
                                           "8,dest_ip,STRING\n" +
                                           "9,nat_src_ip,STRING\n" +
                                           "10,nat_dest_ip,STRING\n" +
                                           "11,rule_name,STRING\n" +
                                           "12,source_user,STRING\n" +
                                           "13,destination_user,STRING\n" +
                                           "14,application,STRING\n" +
                                           "15,virtual_system,STRING\n" +
                                           "16,source_zone,STRING\n" +
                                           "17,destination_zone,STRING\n" +
                                           "18,inbound_interface,STRING\n" +
                                           "19,outbound_interface,STRING\n" +
                                           "20,log_action,STRING\n" +
                                           "21,future_use2,STRING\n" +
                                           "22,session_id,LONG\n" +
                                           "23,repeat_count,LONG\n" +
                                           "24,src_port,LONG\n" +
                                           "25,dest_port,LONG\n" +
                                           "26,nat_src_port,LONG\n" +
                                           "27,nat_dest_port,LONG\n" +
                                           "28,flags,STRING\n" +
                                           "29,protocol,STRING\n" +
                                           "30,action,STRING\n" +
                                           "31,miscellaneous,STRING\n" +
                                           "32,threat_id,STRING\n" +
                                           "33,category,STRING\n" +
                                           "34,severity,STRING\n" +
                                           "35,direction,STRING\n" +
                                           "36,sequence_number,STRING\n" +
                                           "37,action_flags,STRING\n" +
                                           "38,source_location,STRING\n" +
                                           "39,destination_location,STRING\n" +
                                           "40,future_use3,STRING\n" +
                                           "41,content_type,STRING\n" +
                                           "42,pcap_id,STRING\n" +
                                           "43,file_digest,STRING\n" +
                                           "44,cloud,STRING\n" +
                                           "45,url_index,LONG\n" +
                                           "46,user_agent,STRING\n" +
                                           "47,file_type,STRING\n" +
                                           "48,x-forwarded-for,STRING\n" +
                                           "49,referer,STRING\n" +
                                           "50,sender,STRING\n" +
                                           "51,subject,STRING\n" +
                                           "52,recipient,STRING\n" +
                                           "53,report_id,LONG\n" +
                                           "54,device_group_hierarchy_l1,LONG\n" +
                                           "55,device_group_hierarchy_l2,LONG\n" +
                                           "56,device_group_hierarchy_l3,LONG\n" +
                                           "57,device_group_hierarchy_l4,LONG\n" +
                                           "58,virtual_system_name,STRING\n" +
                                           "59,device_name,STRING\n" +
                                           "60,future_use4,STRING\n" +
                                           "61,source_vm_uuid,STRING\n" +
                                           "62,destination_vm_uuid,STRING\n" +
                                           "63,http_method,STRING\n" +
                                           "64,tunnel_id_imsi,STRING\n" +
                                           "65,monitor_tag_imei,STRING\n" +
                                           "66,parent_session_id,STRING\n" +
                                           "67,parent_start_time,STRING\n" +
                                           "68,tunnel_type,STRING\n" +
                                           "69,threat_category,STRING\n" +
                                           "70,content_version,STRING\n" +
                                           "71,future_use5,STRING\n" +
                                           "72,sctp_association_id,LONG\n" +
                                           "73,payload_protocol_id,LONG\n" +
                                           "74,http_headers,STRING";

    public static String TRAFFIC_TEMPLATE = headerRow() +
                                            "1,receive_time,STRING\n" +
                                            "2,serial_number,STRING\n" +
                                            "3,type,STRING\n" +
                                            "4,threat_content_type,STRING\n" +
                                            "5,future_use1,STRING\n" +
                                            "6,generated_time,STRING\n" +
                                            "7,src_ip,STRING\n" +
                                            "8,dest_ip,STRING\n" +
                                            "9,nat_src_ip,STRING\n" +
                                            "10,nat_dest_ip,STRING\n" +
                                            "11,rule_name,STRING\n" +
                                            "12,source_user,STRING\n" +
                                            "13,destination_user,STRING\n" +
                                            "14,application,STRING\n" +
                                            "15,virtual_system,STRING\n" +
                                            "16,source_zone,STRING\n" +
                                            "17,destination_zone,STRING\n" +
                                            "18,inbound_interface,STRING\n" +
                                            "19,outbound_interface,STRING\n" +
                                            "20,log_action,STRING\n" +
                                            "21,future_use2,STRING\n" +
                                            "22,session_id,LONG\n" +
                                            "23,repeat_count,LONG\n" +
                                            "24,source_port,LONG\n" +
                                            "25,destination_port,LONG\n" +
                                            "26,nat_source_port,LONG\n" +
                                            "27,nat_destination_port,LONG\n" +
                                            "28,flags,STRING\n" +
                                            "29,protocol,STRING\n" +
                                            "30,action,STRING\n" +
                                            "31,bytes,LONG\n" +
                                            "32,bytes_sent,LONG\n" +
                                            "33,bytes_received,LONG\n" +
                                            "34,packets,LONG\n" +
                                            "35,start_time,STRING\n" +
                                            "36,elapsed_time,STRING\n" +
                                            "37,category,STRING\n" +
                                            "38,future_use3,STRING\n" +
                                            "39,sequence_number,STRING\n" +
                                            "40,action_flags,STRING\n" +
                                            "41,source_location,STRING\n" +
                                            "42,destination_location,STRING\n" +
                                            "43,future_use4,STRING\n" +
                                            "44,packets_sent,LONG\n" +
                                            "45,packets_received,LONG\n" +
                                            "46,session_end_reason,STRING\n" +
                                            "47,device_group_hierarchy_l1,STRING\n" +
                                            "48,device_group_hierarchy_l2,STRING\n" +
                                            "49,device_group_hierarchy_l3,STRING\n" +
                                            "50,device_group_hierarchy_l4,STRING\n" +
                                            "51,virtual_system_name,STRING\n" +
                                            "52,device_name,STRING\n" +
                                            "53,action_source,STRING\n" +
                                            "54,source_vm_uuid,STRING\n" +
                                            "55,destination_vm_uuid,STRING\n" +
                                            "56,tunnel_id_imsi,STRING\n" +
                                            "57,monitor_tag_imei,STRING\n" +
                                            "58,parent_session_id,STRING\n" +
                                            "59,parent_start_time,STRING\n" +
                                            "60,tunnel_type,STRING\n" +
                                            "61,sctp_association_id,STRING\n" +
                                            "62,sctp_chunks,STRING\n" +
                                            "63,sctp_chunks_sent,STRING\n" +
                                            "64,sctp_chunks_received,STRING";
}