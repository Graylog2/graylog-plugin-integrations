package org.graylog.integrations.inputs.paloalto.types;

/**
 * Default PAN message templates. Used for tests and initial configurations on input creation.
 *
 * These should be maintained to always reflect the latest PAN message versions.
 *
 * I believe these currently are for version 8.1
 *
 * TODO: Consider moving this JSON to static resource fields.
 */
public class PANTemplateDefaults {

    public static String POSITION = "position";
    public static String FIELD = "field";
    public static  String TYPE = "type";

    private PANTemplateDefaults() {

    }

    public static String SYSTEM_TEMPLATE = headerRow() +
                                           "1,pa_time_received,STRING\n" +
                                           "2,serial_number,STRING\n" +
                                           "3,pa_type,STRING\n" +
                                           "4,content_type,STRING\n" +
                                           "6,pa_time_generated,STRING\n" +
                                           "7,virtual_system,STRING\n" +
                                           "8,event_id,STRING\n" +
                                           "9,object,STRING\n" +
                                           "12,module,STRING\n" +
                                           "13,pa_severity,STRING\n" +
                                           "14,description,STRING\n" +
                                           "21,pa_virtualsys_name,STRING\n" +
                                           "22,pa_devicename,STRING";

    private static String headerRow() {
        return POSITION + "," + FIELD + "," + TYPE + "\n";
    }

    public static String THREAT_TEMPLATE = headerRow() +
                                           "1,pa_time_received,STRING\n" +
                                           "2,serial_number,STRING\n" +
                                           "3,pa_type,STRING\n" +
                                           "4,threat_type,STRING\n" +
                                           "5,pa_unknown,STRING\n" +
                                           "6,generated_time,STRING\n" +
                                           "7,src_ip,STRING\n" +
                                           "8,dest_ip,STRING\n" +
                                           "9,nat_src_ip,STRING\n" +
                                           "10,nat_dest_ip,STRING\n" +
                                           "11,pa_rule_name,STRING\n" +
                                           "12,pa_src_user,STRING\n" +
                                           "13,pa_dest_user,STRING\n" +
                                           "14,pa_application,STRING\n" +
                                           "15,pa_virtualsys,STRING\n" +
                                           "16,pa_src_zone,STRING\n" +
                                           "17,pa_dest_zone,STRING\n" +
                                           "18,pa_inbound_int,STRING\n" +
                                           "19,pa_outbound_int,STRING\n" +
                                           "20,pa_log_action,STRING\n" +
                                           "21,pa_future_use0,STRING\n" +
                                           "22,pa_session_id,LONG\n" +
                                           "23,pa_repeat_count,LONG\n" +
                                           "24,src_port,LONG\n" +
                                           "25,dest_port,LONG\n" +
                                           "26,nat_src_port,LONG\n" +
                                           "27,nat_dest_port,LONG\n" +
                                           "28,pa_flags,STRING\n" +
                                           "29,protocol,STRING\n" +
                                           "30,action,STRING\n" +
                                           "31,pa_misc,STRING\n" +
                                           "32,pa_threatID,STRING\n" +
                                           "33,pa_noname,STRING\n" +
                                           "34,pa_severity,STRING\n" +
                                           "35,pa_direction,STRING\n" +
                                           "36,pa_seqnum,LONG\n" +
                                           "37,action_flags,STRING\n" +
                                           "38,pa_src_location,STRING\n" +
                                           "39,pa_dest_location,STRING\n" +
                                           "40,future_use,STRING\n" +
                                           "41,content_type,STRING\n" +
                                           "42,pcap_id,LONG\n" +
                                           "43,pa_file_digest,STRING\n" +
                                           "44,pa_cloud,STRING\n" +
                                           "45,url_index,LONG\n" +
                                           "46,user_agent,STRING\n" +
                                           "47,file_type,STRING\n" +
                                           "49,x-forwarded-for,STRING\n" +
                                           "50,http_referer,STRING\n" +
                                           "51,email_sender,STRING\n" +
                                           "52,email_subject,STRING\n" +
                                           "53,email_recipient,STRING\n" +
                                           "54,report_id,LONG\n" +
                                           "55,dvc_hierarchy_l1,LONG\n" +
                                           "56,dvc_hierarchy_l2,LONG\n" +
                                           "57,dvc_hierarchy_l3,LONG\n" +
                                           "58,dvc_hierarchy_l4,LONG\n" +
                                           "59,pa_virtualsys_name,STRING\n" +
                                           "60,pa_devicename,STRING\n" +
                                           "61,pa_src_vm_uuid,STRING\n" +
                                           "62,pa_dest_vm_uuid,STRING\n" +
                                           "63,http_method,STRING\n" +
                                           "64,pa_tunnel_id_imsi,LONG\n" +
                                           "65,pa_monitortag_imei,STRING\n" +
                                           "66,pa_parent_session_id,LONG\n" +
                                           "67,pa_parent_start_time,STRING\n" +
                                           "68,pa_tunnel_type,STRING\n" +
                                           "69,threat_category,STRING\n" +
                                           "70,pa_content_version,STRING\n" +
                                           "71,future_use_end,STRING\n" +
                                           "72,pa_sctp_assoc_id,LONG\n" +
                                           "73,pa_payload_prot_id,LONG\n" +
                                           "74,http_headers,STRING";

    public static String TRAFFIC_TEMPLATE = headerRow() +
                                            "1,pa_time_received,STRING\n" +
                                            "2,serial_number,STRING\n" +
                                            "3,pa_type,STRING\n" +
                                            "4,content_type,STRING\n" +
                                            "6,pa_time_generated,STRING\n" +
                                            "7,src_ip,STRING\n" +
                                            "8,dest_ip,STRING\n" +
                                            "9,nat_src_ip,STRING\n" +
                                            "10,nat_dest_ip,STRING\n" +
                                            "11,rule_name,STRING\n" +
                                            "12,src_user,STRING\n" +
                                            "13,dest_user,STRING\n" +
                                            "14,application,STRING\n" +
                                            "15,pa_virtualsys_name,STRING\n" +
                                            "16,src_zone,STRING\n" +
                                            "17,dst_zone,STRING\n" +
                                            "18,interface_inbound,STRING\n" +
                                            "19,interface_outbound,STRING\n" +
                                            "20,log_action,STRING\n" +
                                            "22,session_id,LONG\n" +
                                            "23,repeat_count,LONG\n" +
                                            "24,src_port,LONG\n" +
                                            "25,dest_port,LONG\n" +
                                            "26,nat_src_port,LONG\n" +
                                            "27,nat_dest_port,LONG\n" +
                                            "29,protocol,STRING\n" +
                                            "30,action,STRING\n" +
                                            "31,bytes,LONG\n" +
                                            "32,bytes_sent,LONG\n" +
                                            "33,bytes_received,LONG\n" +
                                            "34,packets,LONG\n" +
                                            "37,category,STRING\n" +
                                            "41,src_location,STRING\n" +
                                            "42,dest_location,STRING\n" +
                                            "44,packets_sent,LONG\n" +
                                            "45,packets_received,LONG\n" +
                                            "51,pa_devicename,STRING";
}