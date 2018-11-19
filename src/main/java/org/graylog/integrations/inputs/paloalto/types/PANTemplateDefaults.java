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

    private PANTemplateDefaults() {

    }

    static String SYSTEM_TEMPLATE = "{ " +
                                           "  \"fields\": [ " +
                                           "    { \"position\": 1, \"field\": \"pa_time_received\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 2, \"field\": \"serial_number\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 3, \"field\": \"pa_type\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 4, \"field\": \"content_type\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 6, \"field\": \"pa_time_generated\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 7, \"field\": \"virtual_system\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 8, \"field\": \"event_id\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 9, \"field\": \"object\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 12, \"field\": \"module\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 13, \"field\": \"pa_severity\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 14, \"field\": \"description\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 21, \"field\": \"pa_virtualsys_name\", \"type\": \"STRING\"}, " +
                                           "    { \"position\": 22, \"field\": \"pa_devicename\", \"type\": \"STRING\"} " +
                                           "  ] " +
                                           "}";

    static String THREAT_TEMPLATE = "{ " +
                                           "  \"fields\": [" +
                                           "    {\"position\": 1,  \"field\": \"pa_time_received\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 2,  \"field\": \"serial_number\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 3,  \"field\": \"pa_type\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 4,  \"field\": \"threat_type\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 5,  \"field\": \"pa_unknown\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 6,  \"field\": \"generated_time\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 7,  \"field\": \"src_ip\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 8,  \"field\": \"dest_ip\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 9,  \"field\": \"nat_src_ip\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 10, \"field\": \"nat_dest_ip\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 11, \"field\": \"pa_rule_name\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 12, \"field\": \"pa_src_user\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 13, \"field\": \"pa_dest_user\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 14, \"field\": \"pa_application\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 15, \"field\": \"pa_virtualsys\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 16, \"field\": \"pa_src_zone\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 17, \"field\": \"pa_dest_zone\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 18, \"field\": \"pa_inbound_int\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 19, \"field\": \"pa_outbound_int\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 20, \"field\": \"pa_log_action\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 21, \"field\": \"pa_future_use0\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 22, \"field\": \"pa_session_id\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 23, \"field\": \"pa_repeat_count\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 24, \"field\": \"src_port\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 25, \"field\": \"dest_port\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 26, \"field\": \"nat_src_port\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 27, \"field\": \"nat_dest_port\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 28, \"field\": \"pa_flags\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 29, \"field\": \"protocol\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 30, \"field\": \"action\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 31, \"field\": \"pa_misc\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 32, \"field\": \"pa_threatID\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 33, \"field\": \"pa_noname\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 34, \"field\": \"pa_severity\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 35, \"field\": \"pa_direction\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 36, \"field\": \"pa_seqnum\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 37, \"field\": \"action_flags\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 38, \"field\": \"pa_src_location\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 39, \"field\": \"pa_dest_location\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 40, \"field\": \"future_use\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 41, \"field\": \"content_type\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 42, \"field\": \"pcap_id\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 43, \"field\": \"pa_file_digest\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 44, \"field\": \"pa_cloud\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 45, \"field\": \"url_index\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 46, \"field\": \"user_agent\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 47, \"field\": \"file_type\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 49, \"field\": \"x-forwarded-for\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 50, \"field\": \"http_referer\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 51, \"field\": \"email_sender\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 52, \"field\": \"email_subject\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 53, \"field\": \"email_recipient\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 54, \"field\": \"report_id\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 55, \"field\": \"dvc_hierarchy_l1\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 56, \"field\": \"dvc_hierarchy_l2\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 57, \"field\": \"dvc_hierarchy_l3\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 58, \"field\": \"dvc_hierarchy_l4\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 59, \"field\": \"pa_virtualsys_name\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 60, \"field\": \"pa_devicename\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 61, \"field\": \"pa_src_vm_uuid\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 62, \"field\": \"pa_dest_vm_uuid\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 63, \"field\": \"http_method\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 64, \"field\": \"pa_tunnel_id_imsi\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 65, \"field\": \"pa_monitortag_imei\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 66, \"field\": \"pa_parent_session_id\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 67, \"field\": \"pa_parent_start_time\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 68, \"field\": \"pa_tunnel_type\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 69, \"field\": \"threat_category\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 70, \"field\": \"pa_content_version\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 71, \"field\": \"future_use_end\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 72, \"field\": \"pa_sctp_assoc_id\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 73, \"field\": \"pa_payload_prot_id\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 74, \"field\": \"http_headers\", \"type\": \"STRING\"}" +
                                           " ]}" +
                                           "}";

    static String TRAFFIC_TEMPLATE = "{ " +
                                           "  \"fields\": [ " +
                                           "    {\"position\": 1,\"field\":\"pa_time_received\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 2,\"field\":\"serial_number\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 3,\"field\":\"pa_type\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 4,\"field\":\"content_type\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 6,\"field\":\"pa_time_generated\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 7,\"field\":\"src_ip\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 8,\"field\":\"dest_ip\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 9,\"field\":\"nat_src_ip\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 10,\"field\":\"nat_dest_ip\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 11,\"field\":\"rule_name\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 12,\"field\":\"src_user\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 13,\"field\":\"dest_user\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 14,\"field\":\"application\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 15,\"field\":\"pa_virtualsys_name\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 16,\"field\":\"src_zone\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 17,\"field\":\"dst_zone\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 18,\"field\":\"interface_inbound\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 19,\"field\":\"interface_outbound\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 20,\"field\":\"log_action\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 22,\"field\":\"session_id\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 23,\"field\":\"repeat_count\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 24,\"field\":\"src_port\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 25,\"field\":\"dest_port\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 26,\"field\":\"nat_src_port\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 27,\"field\":\"nat_dest_port\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 29,\"field\":\"protocol\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 30,\"field\":\"action\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 31,\"field\":\"bytes\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 32,\"field\":\"bytes_sent\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 33,\"field\":\"bytes_received\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 34,\"field\":\"packets\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 37,\"field\":\"category\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 41,\"field\":\"src_location\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 42,\"field\":\"dest_location\", \"type\": \"STRING\"}, " +
                                           "    {\"position\": 44,\"field\":\"packets_sent\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 45,\"field\":\"packets_received\", \"type\": \"LONG\"}, " +
                                           "    {\"position\": 51,\"field\":\"pa_devicename\", \"type\": \"STRING\"}" +
                                           " ]}";
}