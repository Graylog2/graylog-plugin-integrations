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
                                           "1,pa_receive_time,STRING\n" +
                                           "2,pa_serial_number,STRING\n" +
                                           "3,pa_type,STRING\n" +
                                           "4,pa_content_threat_type,STRING\n" +
                                           "5,pa_future_use1,STRING\n" +
                                           "6,pa_generated_time,STRING\n" +
                                           "7,pa_virtual_system,STRING\n" +
                                           "8,pa_event_id,STRING\n" +
                                           "9,pa_object,STRING\n" +
                                           "10,pa_future_use2,STRING\n" +
                                           "11,pa_future_use3,STRING\n" +
                                           "12,pa_module,STRING\n" +
                                           "13,pa_severity,STRING\n" +
                                           "14,pa_description,STRING\n" +
                                           "15,pa_sequence_number,STRING\n" +
                                           "16,pa_action_flags,STRING\n" +
                                           "17,pa_device_group_hierarchy_l1,STRING\n" +
                                           "18,pa_device_group_hierarchy_l2,STRING\n" +
                                           "19,pa_device_group_hierarchy_l3,STRING\n" +
                                           "20,pa_device_group_hierarchy_l4,STRING\n" +
                                           "21,pa_virtual_system_name,STRING\n" +
                                           "22,pa_device_name,STRING";

    private static String headerRow() {
        return POSITION + "," + FIELD + "," + TYPE + "\n";
    }

    public static String THREAT_TEMPLATE = headerRow() +
                                           "1,pa_receive_time,STRING\n" +
                                           "2,pa_serial_number,STRING\n" +
                                           "3,pa_type,STRING\n" +
                                           "4,pa_threat_content_type,STRING\n" +
                                           "5,pa_future_use1,STRING\n" +
                                           "6,pa_generated_time,STRING\n" +
                                           "7,src_ip,STRING\n" +
                                           "8,dest_ip,STRING\n" +
                                           "9,nat_src_ip,STRING\n" +
                                           "10,nat_dest_ip,STRING\n" +
                                           "11,pa_rule_name,STRING\n" +
                                           "12,pa_source_user,STRING\n" +
                                           "13,pa_destination_user,STRING\n" +
                                           "14,pa_application,STRING\n" +
                                           "15,pa_virtual_system,STRING\n" +
                                           "16,pa_source_zone,STRING\n" +
                                           "17,pa_destination_zone,STRING\n" +
                                           "18,pa_inbound_interface,STRING\n" +
                                           "19,pa_outbound_interface,STRING\n" +
                                           "20,pa_log_action,STRING\n" +
                                           "21,pa_future_use2,STRING\n" +
                                           "22,pa_session_id,LONG\n" +
                                           "23,pa_repeat_count,LONG\n" +
                                           "24,src_port,LONG\n" +
                                           "25,dest_port,LONG\n" +
                                           "26,nat_src_port,LONG\n" +
                                           "27,nat_dest_port,LONG\n" +
                                           "28,pa_flags,STRING\n" +
                                           "29,pa_protocol,STRING\n" +
                                           "30,pa_action,STRING\n" +
                                           "31,pa_miscellaneous,STRING\n" +
                                           "32,pa_threat_id,STRING\n" +
                                           "33,pa_category,STRING\n" +
                                           "34,pa_severity,STRING\n" +
                                           "35,pa_direction,STRING\n" +
                                           "36,pa_sequence_number,LONG\n" +
                                           "37,pa_action_flags,STRING\n" +
                                           "38,pa_source_location,STRING\n" +
                                           "39,pa_destination_location,STRING\n" +
                                           "40,pa_future_use3,STRING\n" +
                                           "41,pa_content_type,STRING\n" +
                                           "42,pa_pcap_id,LONG\n" +
                                           "43,pa_file_digest,STRING\n" +
                                           "44,pa_cloud,STRING\n" +
                                           "45,pa_url_index,LONG\n" +
                                           "46,pa_user_agent,STRING\n" +
                                           "47,pa_file_type,STRING\n" +
                                           "48,pa_x-forwarded-for,STRING\n" +
                                           "49,pa_referer,STRING\n" +
                                           "50,pa_sender,STRING\n" +
                                           "51,pa_subject,STRING\n" +
                                           "52,pa_recipient,STRING\n" +
                                           "53,pa_report_id,LONG\n" +
                                           "54,pa_device_group_hierarchy_l1,LONG\n" +
                                           "55,pa_device_group_hierarchy_l2,LONG\n" +
                                           "56,pa_device_group_hierarchy_l3,LONG\n" +
                                           "57,pa_device_group_hierarchy_l4,LONG\n" +
                                           "58,pa_virtual_system_name,STRING\n" +
                                           "59,pa_device_name,STRING\n" +
                                           "60,pa_future_use4,STRING\n" +
                                           "61,pa_source_vm_uuid,STRING\n" +
                                           "62,pa_destination_vm_uuid,STRING\n" +
                                           "63,pa_http_method,STRING\n" +
                                           "64,pa_tunnel_id_imsi,STRING\n" +
                                           "65,pa_monitor_tag_imei,STRING\n" +
                                           "66,pa_parent_session_id,STRING\n" +
                                           "67,pa_parent_start_time,STRING\n" +
                                           "68,pa_tunnel_type,STRING\n" +
                                           "69,pa_threat_category,STRING\n" +
                                           "70,pa_content_version,STRING\n" +
                                           "71,pa_future_use5,STRING\n" +
                                           "72,pa_sctp_association_id,LONG\n" +
                                           "73,pa_payload_protocol_id,LONG\n" +
                                           "74,pa_http_headers,STRING";

    public static String TRAFFIC_TEMPLATE = headerRow() +
                                            "1,pa_receive_time,STRING\n" +
                                            "2,pa_serial_number,STRING\n" +
                                            "3,pa_type,STRING\n" +
                                            "4,pa_threat_content_type,STRING\n" +
                                            "5,pa_future_use1,\n" +
                                            "6,pa_generated_time,STRING\n" +
                                            "7,src_ip,STRING\n" +
                                            "8,dest_ip,STRING\n" +
                                            "9,nat_src_ip,STRING\n" +
                                            "10,nat_dest_ip,STRING\n" +
                                            "11,pa_rule_name,STRING\n" +
                                            "12,pa_source_user,STRING\n" +
                                            "13,pa_destination_user,STRING\n" +
                                            "14,pa_application,STRING\n" +
                                            "15,pa_virtual_system,STRING\n" +
                                            "16,pa_source_zone,STRING\n" +
                                            "17,pa_destination_zone,STRING\n" +
                                            "18,pa_inbound_interface,STRING\n" +
                                            "19,pa_outbound_interface,STRING\n" +
                                            "20,pa_log_action,STRING\n" +
                                            "21,pa_future_use2,STRING\n" +
                                            "22,pa_session_id,LONG\n" +
                                            "23,pa_repeat_count,LONG\n" +
                                            "24,pa_source_port,LONG\n" +
                                            "25,pa_destination_port,LONG\n" +
                                            "26,pa_nat_source_port,LONG\n" +
                                            "27,pa_nat_destination_port,LONG\n" +
                                            "28,pa_flags,STRING\n" +
                                            "29,pa_protocol,STRING\n" +
                                            "30,pa_action,STRING\n" +
                                            "31,pa_bytes,LONG\n" +
                                            "32,pa_bytes_sent,LONG\n" +
                                            "33,pa_bytes_received,LONG\n" +
                                            "34,pa_packets,LONG\n" +
                                            "35,pa_start_time,STRING\n" +
                                            "36,pa_elapsed_time,STRING\n" +
                                            "37,pa_category,STRING\n" +
                                            "38,pa_future_use3,STRING\n" +
                                            "39,pa_sequence_number,STRING\n" +
                                            "40,pa_action_flags,STRING\n" +
                                            "41,pa_source_location,STRING\n" +
                                            "42,pa_destination_location,STRING\n" +
                                            "43,pa_future_use4,STRING\n" +
                                            "44,pa_packets_sent,LONG\n" +
                                            "45,pa_packets_received,LONG\n" +
                                            "46,pa_session_end_reason,STRING\n" +
                                            "47,pa_device_group_hierarchy_l1,STRING\n" +
                                            "48,pa_device_group_hierarchy_l2,STRING\n" +
                                            "49,pa_device_group_hierarchy_l3,STRING\n" +
                                            "50,pa_device_group_hierarchy_l4,STRING\n" +
                                            "51,pa_virtual_system_name,STRING\n" +
                                            "52,pa_device_name,STRING\n" +
                                            "53,pa_action_source,STRING\n" +
                                            "54,pa_source_vm_uuid,STRING\n" +
                                            "55,pa_destination_vm_uuid,STRING\n" +
                                            "56,pa_tunnel_id_imsi,STRING\n" +
                                            "57,pa_monitor_tag_imei,STRING\n" +
                                            "58,pa_parent_session_id,STRING\n" +
                                            "59,pa_parent_start_time,STRING\n" +
                                            "60,pa_tunnel_type,STRING\n" +
                                            "61,pa_sctp_association_id,STRING\n" +
                                            "62,pa_sctp_chunks,STRING\n" +
                                            "63,pa_sctp_chunks_sent,STRING\n" +
                                            "64,pa_sctp_chunks_received,STRING";
}