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

    public static String SYSTEM_TEMPLATE = "{ \n" +
                                           "  \"fields\": [ \n" +
                                           "    { \"position\": 1, \"field\": \"pa_time_received\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 2, \"field\": \"serial_number\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 3, \"field\": \"pa_type\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 4, \"field\": \"content_type\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 6, \"field\": \"pa_time_generated\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 7, \"field\": \"virtual_system\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 8, \"field\": \"event_id\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 9, \"field\": \"object\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 12, \"field\": \"module\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 13, \"field\": \"pa_severity\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 14, \"field\": \"description\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 21, \"field\": \"pa_virtualsys_name\", \"type\": \"STRING\"}, \n" +
                                           "    { \"position\": 22, \"field\": \"pa_devicename\", \"type\": \"STRING\"} \n" +
                                           "  ] \n" +
                                           "}";

    public static String THREAT_TEMPLATE = "{ \n" +
                                           "  \"fields\": [\n" +
                                           "    {\"position\": 1,  \"field\": \"pa_time_received\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 2,  \"field\": \"serial_number\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 3,  \"field\": \"pa_type\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 4,  \"field\": \"threat_type\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 5,  \"field\": \"pa_unknown\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 6,  \"field\": \"generated_time\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 7,  \"field\": \"src_ip\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 8,  \"field\": \"dest_ip\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 9,  \"field\": \"nat_src_ip\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 10, \"field\": \"nat_dest_ip\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 11, \"field\": \"pa_rule_name\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 12, \"field\": \"pa_src_user\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 13, \"field\": \"pa_dest_user\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 14, \"field\": \"pa_application\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 15, \"field\": \"pa_virtualsys\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 16, \"field\": \"pa_src_zone\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 17, \"field\": \"pa_dest_zone\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 18, \"field\": \"pa_inbound_int\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 19, \"field\": \"pa_outbound_int\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 20, \"field\": \"pa_log_action\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 21, \"field\": \"pa_future_use0\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 22, \"field\": \"pa_session_id\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 23, \"field\": \"pa_repeat_count\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 24, \"field\": \"src_port\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 25, \"field\": \"dest_port\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 26, \"field\": \"nat_src_port\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 27, \"field\": \"nat_dest_port\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 28, \"field\": \"pa_flags\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 29, \"field\": \"protocol\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 30, \"field\": \"action\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 31, \"field\": \"pa_misc\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 32, \"field\": \"pa_threatID\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 33, \"field\": \"pa_noname\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 34, \"field\": \"pa_severity\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 35, \"field\": \"pa_direction\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 36, \"field\": \"pa_seqnum\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 37, \"field\": \"action_flags\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 38, \"field\": \"pa_src_location\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 39, \"field\": \"pa_dest_location\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 40, \"field\": \"future_use\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 41, \"field\": \"content_type\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 42, \"field\": \"pcap_id\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 43, \"field\": \"pa_file_digest\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 44, \"field\": \"pa_cloud\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 45, \"field\": \"url_index\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 46, \"field\": \"user_agent\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 47, \"field\": \"file_type\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 49, \"field\": \"x-forwarded-for\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 50, \"field\": \"http_referer\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 51, \"field\": \"email_sender\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 52, \"field\": \"email_subject\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 53, \"field\": \"email_recipient\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 54, \"field\": \"report_id\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 55, \"field\": \"dvc_hierarchy_l1\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 56, \"field\": \"dvc_hierarchy_l2\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 57, \"field\": \"dvc_hierarchy_l3\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 58, \"field\": \"dvc_hierarchy_l4\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 59, \"field\": \"pa_virtualsys_name\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 60, \"field\": \"pa_devicename\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 61, \"field\": \"pa_src_vm_uuid\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 62, \"field\": \"pa_dest_vm_uuid\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 63, \"field\": \"http_method\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 64, \"field\": \"pa_tunnel_id_imsi\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 65, \"field\": \"pa_monitortag_imei\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 66, \"field\": \"pa_parent_session_id\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 67, \"field\": \"pa_parent_start_time\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 68, \"field\": \"pa_tunnel_type\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 69, \"field\": \"threat_category\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 70, \"field\": \"pa_content_version\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 71, \"field\": \"future_use_end\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 72, \"field\": \"pa_sctp_assoc_id\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 73, \"field\": \"pa_payload_prot_id\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 74, \"field\": \"http_headers\", \"type\": \"STRING\"}\n" +
                                           " ]}\n" +
                                           "}";

    public static String TRAFFIC_TEMPLATE = "{ \n" +
                                           "  \"fields\": [ \n" +
                                           "    {\"position\": 1,\"field\":\"pa_time_received\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 2,\"field\":\"serial_number\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 3,\"field\":\"pa_type\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 4,\"field\":\"content_type\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 6,\"field\":\"pa_time_generated\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 7,\"field\":\"src_ip\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 8,\"field\":\"dest_ip\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 9,\"field\":\"nat_src_ip\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 10,\"field\":\"nat_dest_ip\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 11,\"field\":\"rule_name\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 12,\"field\":\"src_user\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 13,\"field\":\"dest_user\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 14,\"field\":\"application\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 15,\"field\":\"pa_virtualsys_name\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 16,\"field\":\"src_zone\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 17,\"field\":\"dst_zone\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 18,\"field\":\"interface_inbound\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 19,\"field\":\"interface_outbound\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 20,\"field\":\"log_action\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 22,\"field\":\"session_id\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 23,\"field\":\"repeat_count\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 24,\"field\":\"src_port\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 25,\"field\":\"dest_port\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 26,\"field\":\"nat_src_port\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 27,\"field\":\"nat_dest_port\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 29,\"field\":\"protocol\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 30,\"field\":\"action\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 31,\"field\":\"bytes\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 32,\"field\":\"bytes_sent\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 33,\"field\":\"bytes_received\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 34,\"field\":\"packets\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 37,\"field\":\"category\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 41,\"field\":\"src_location\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 42,\"field\":\"dest_location\", \"type\": \"STRING\"}, \n" +
                                           "    {\"position\": 44,\"field\":\"packets_sent\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 45,\"field\":\"packets_received\", \"type\": \"LONG\"}, \n" +
                                           "    {\"position\": 51,\"field\":\"pa_devicename\", \"type\": \"STRING\"}\n" +
                                           " ]}";
}