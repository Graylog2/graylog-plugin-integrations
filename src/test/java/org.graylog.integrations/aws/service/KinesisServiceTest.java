package org.graylog.integrations.aws.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AssertionsForClassTypes;
import org.graylog.integrations.aws.AWSLogMessage;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.codecs.CloudWatchFlowLogCodec;
import org.graylog.integrations.aws.codecs.CloudWatchRawLogCodec;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorResponse;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsResponse;
import software.amazon.awssdk.services.kinesis.model.ListStreamsRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsResponse;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.Shard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

public class KinesisServiceTest {

    private static final String TEST_STREAM_1 = "test-stream-1";
    private static final String TEST_STREAM_2 = "test-stream-2";
    private static final String[] TWO_TEST_STREAMS = {TEST_STREAM_1, TEST_STREAM_2};
    private static final String TEST_REGION = Region.EU_WEST_1.id();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private KinesisClientBuilder kinesisClientBuilder;

    @Mock
    private KinesisClient kinesisClient;

    private KinesisService kinesisService;

    private Map<String, Codec.Factory<? extends Codec>> availableCodecs;

    @Before
    public void setUp() {

        // Create an AWS client with a mock KinesisClientBuilder
        availableCodecs = new HashMap<>();

        // Prepare test codecs. These have to be manually instantiated for the test context.
        availableCodecs.put(CloudWatchRawLogCodec.NAME, new CloudWatchRawLogCodec.Factory() {
            @Override
            public CloudWatchRawLogCodec create(Configuration configuration) {
                return new CloudWatchRawLogCodec(configuration, new ObjectMapper());
            }

            @Override
            public CloudWatchRawLogCodec.Config getConfig() {
                return null;
            }

            @Override
            public Codec.Descriptor getDescriptor() {
                return null;
            }
        });

        availableCodecs.put(CloudWatchFlowLogCodec.NAME, new CloudWatchFlowLogCodec.Factory() {
            @Override
            public CloudWatchFlowLogCodec create(Configuration configuration) {
                return new CloudWatchFlowLogCodec(configuration, new ObjectMapper());
            }

            @Override
            public CloudWatchFlowLogCodec.Config getConfig() {
                return null;
            }

            @Override
            public Codec.Descriptor getDescriptor() {
                return null;
            }
        });

        kinesisService = new KinesisService(kinesisClientBuilder, new ObjectMapper(), availableCodecs);
    }

    @Test
    public void testLogIdentification() {

        // Verify that an ACCEPT flow log us detected as a flow log.
        AWSLogMessage logMessage = new AWSLogMessage("2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 ACCEPT OK");
        assertEquals(AWSMessageType.KINESIS_FLOW_LOGS, logMessage.detectLogMessageType());

        // Verify that an ACCEPT flow log us detected as a flow log.
        logMessage = new AWSLogMessage("2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 REJECT OK");
        assertEquals(AWSMessageType.KINESIS_FLOW_LOGS, logMessage.detectLogMessageType());

        // Verify that a message with 14 spaces (instead of 13) is not identified as a flow log.
        logMessage = new AWSLogMessage("2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 REJECT OK ONE-MORE-WORD");
        assertEquals(AWSMessageType.KINESIS_RAW, logMessage.detectLogMessageType());

        // Verify that a message with 12 spaces (instead of 13) is not identified as a flow log.
        logMessage = new AWSLogMessage("2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 REJECT");
        assertEquals(AWSMessageType.KINESIS_RAW, logMessage.detectLogMessageType());

        // Verify that it's detected as unknown
        logMessage = new AWSLogMessage("haha this is not a real log message");
        assertEquals(AWSMessageType.KINESIS_RAW, logMessage.detectLogMessageType());
    }

    @Test
    public void healthCheckFlowLog() throws ExecutionException, IOException {

        KinesisHealthCheckResponse response = executeHealthCheckTest(buildCloudWatchFlowLogPayload());
        assertEquals(AWSMessageType.KINESIS_FLOW_LOGS, response.inputType());
    }

    @Test
    public void healthCheckCloudWatchFlowLog() throws ExecutionException, IOException {

        KinesisHealthCheckResponse response = executeHealthCheckTest(buildCloudWatchRawPayload());
        assertEquals(AWSMessageType.KINESIS_RAW, response.inputType());
    }

    @Test
    public void healthCheckRawKinesisLog() throws ExecutionException, IOException {

        KinesisHealthCheckResponse response = executeHealthCheckTest("This is a test raw log".getBytes());
        assertEquals(AWSMessageType.KINESIS_RAW, response.inputType());
    }

    private KinesisHealthCheckResponse executeHealthCheckTest(byte[] payloadData) throws IOException, ExecutionException {
        // TODO: This test verifies the path that CloudWatch flow logs are being sent.
        //  Add a test for the case when an unknown CloudWatch format is sent, and also
        //  when a non-CloudWatch payload is provided.

        when(kinesisClientBuilder.region(isA(Region.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.credentialsProvider(isA(AwsCredentialsProvider.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);
        when(kinesisClient.listStreams(isA(ListStreamsRequest.class)))
                .thenReturn(ListStreamsResponse.builder()
                                               .streamNames(TWO_TEST_STREAMS)
                                               .hasMoreStreams(false).build());

        Shard shard = Shard.builder().shardId("shardId-1234").build();
        when(kinesisClient.listShards(isA(ListShardsRequest.class)))
                .thenReturn(ListShardsResponse.builder().shards(shard).build());

        when(kinesisClient.getShardIterator(isA(GetShardIteratorRequest.class)))
                .thenReturn(GetShardIteratorResponse.builder().shardIterator("shardIterator").build());

        final Record record = Record.builder().data(SdkBytes.fromByteArray(payloadData)).build();
        when(kinesisClient.getRecords(isA(GetRecordsRequest.class)))
                .thenReturn(GetRecordsResponse.builder().records(record).millisBehindLatest(10000L).build())
                .thenReturn(GetRecordsResponse.builder().records(record).millisBehindLatest(0L).build());

        // TODO: Additional mock prep will be needed when reading from Kinesis is added.
        KinesisHealthCheckRequest request = KinesisHealthCheckRequest.create(Region.EU_WEST_1.id(),
                                                                             "key", "secret", TEST_STREAM_1, "");
        return kinesisService.healthCheck(request);
    }

    /**
     * Build a data payload for a Flow Log CloudWatch Kinesis subscription record.
     *
     * @see <a href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/SubscriptionFilters.html">CloudWatch Subcription Filters</a>
     */
    private byte[] buildCloudWatchFlowLogPayload() throws IOException {

        final String messageData = "{\n" +
                                   "  \"messageType\": \"DATA_MESSAGE\",\n" +
                                   "  \"owner\": \"459220251735\",\n" +
                                   "  \"logGroup\": \"test-flowlogs\",\n" +
                                   "  \"logStream\": \"eni-3423-all\",\n" +
                                   "  \"subscriptionFilters\": [\n" +
                                   "    \"filter\"\n" +
                                   "  ],\n" +
                                   "  \"logEvents\": [\n" +
                                   "    {\n" +
                                   "      \"id\": \"3423\",\n" +
                                   "      \"timestamp\": 1559738144000,\n" +
                                   "      \"message\": \"2 423432432432 eni-3244234 172.1.1.2 172.1.1.2 80 2264 6 1 52 1559738144 1559738204 ACCEPT OK\"\n" +
                                   "    },\n" +
                                   "    {\n" +
                                   "      \"id\": \"3423\",\n" +
                                   "      \"timestamp\": 1559738144000,\n" +
                                   "      \"message\": \"2 423432432432 eni-3244234 172.1.1.2 172.1.1.2 80 2264 6 1 52 1559738144 1559738204 ACCEPT OK\"\n" +
                                   "    }\n" +
                                   "  ]\n" +
                                   "}";

        // Compress the test record, as CloudWatch subscriptions are compressed.
        return compressPayload(messageData);
    }

    /**
     * Build a data payload for a raw CloudWatch Kinesis subscription record.
     *
     * @see <a href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/SubscriptionFilters.html">CloudWatch Subcription Filters</a>
     */
    private byte[] buildCloudWatchRawPayload() throws IOException {

        final String messageData = "{\n" +
                                   "  \"messageType\": \"DATA_MESSAGE\",\n" +
                                   "  \"owner\": \"459220251735\",\n" +
                                   "  \"logGroup\": \"test-flowlogs\",\n" +
                                   "  \"logStream\": \"eni-3423-all\",\n" +
                                   "  \"subscriptionFilters\": [\n" +
                                   "    \"filter\"\n" +
                                   "  ],\n" +
                                   "  \"logEvents\": [\n" +
                                   "    {\n" +
                                   "      \"id\": \"3423\",\n" +
                                   "      \"timestamp\": 1559738144000,\n" +
                                   "      \"message\": \"Just a raw message\"\n" +
                                   "    },\n" +
                                   "    {\n" +
                                   "      \"id\": \"3423\",\n" +
                                   "      \"timestamp\": 1559738144000,\n" +
                                   "      \"message\": \"Just another raw message\"\n" +
                                   "    }\n" +
                                   "  ]\n" +
                                   "}";

        // Compress the test record, as CloudWatch subscriptions are compressed.
        return compressPayload(messageData);
    }

    private byte[] compressPayload(String messageData) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(messageData.getBytes().length);
        final GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(messageData.getBytes());
        gzip.close();
        final byte[] compressed = bos.toByteArray();
        bos.close();
        return compressed;
    }

    @Test
    public void testGetStreamsCredentials() {
        AssertionsForClassTypes.assertThatThrownBy(() -> kinesisService.getKinesisStreams(TEST_REGION, "", ""))
                               .isExactlyInstanceOf(IllegalArgumentException.class)
                               .hasMessageContaining("An AWS access key is required");
        AssertionsForClassTypes.assertThatThrownBy(() -> kinesisService.getKinesisStreams(TEST_REGION, "dsfadsdf", ""))
                               .isExactlyInstanceOf(IllegalArgumentException.class)
                               .hasMessageContaining("An AWS secret key is required");
        AssertionsForClassTypes.assertThatThrownBy(() -> kinesisService.getKinesisStreams(TEST_REGION, "", "dsfadsdf"))
                               .isExactlyInstanceOf(IllegalArgumentException.class)
                               .hasMessageContaining("An AWS access key is required");
    }

    @Test
    public void testGetStreams() throws ExecutionException {

        // Test with two streams and one page. This is the most common case for most AWS accounts.
        when(kinesisClientBuilder.region(isA(Region.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.credentialsProvider(isA(AwsCredentialsProvider.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        when(kinesisClient.listStreams(isA(ListStreamsRequest.class)))
                .thenReturn(ListStreamsResponse.builder()
                                               .streamNames(TWO_TEST_STREAMS)
                                               .hasMoreStreams(false).build());


        List<String> kinesisStreams = kinesisService.getKinesisStreams(TEST_REGION, "accessKey", "secretKey");
        assertEquals(2, kinesisStreams.size());

        // Test with stream paging functionality. This will be the case when a large number of Kinesis streams
        // are present on a particular AWS account.
        when(kinesisClientBuilder.region(isA(Region.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.credentialsProvider(isA(AwsCredentialsProvider.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        when(kinesisClient.listStreams(isA(ListStreamsRequest.class)))
                // First return a response with two streams indicating that there are more.
                .thenReturn(ListStreamsResponse.builder()
                                               .streamNames(TWO_TEST_STREAMS)
                                               .hasMoreStreams(true).build())
                // Then return a response with two streams and indicate that all have been retrieved.
                .thenReturn(ListStreamsResponse.builder()
                                               .streamNames(TWO_TEST_STREAMS)
                                               .hasMoreStreams(false).build()); // Indicate no more streams.

        kinesisStreams = kinesisService.getKinesisStreams(TEST_REGION, "accessKey", "secretKey");

        // There should be 4 total streams (two from each page).
        assertEquals(4, kinesisStreams.size());

    }

    // TODO Add retrieveRecords test

    @Test
    public void testMessageFormat() {

        HashMap<String, Object> fields = new HashMap<>();
        fields.put("_id", "123");
        fields.put("src_addr", "Dan");
        fields.put("port", 80);

        String summary = kinesisService.buildMessageSummary(new Message(fields), "The full message");
        assertEquals("The summary should have 4 lines", 4, summary.split("\n").length);
        assertTrue(summary.contains("id"));
        assertTrue(summary.contains("src_addr"));
        assertTrue(summary.contains("port"));
    }

    @Test
    public void testSelectRandomRecord() {

        // Test empty list
        List<Record> fakeRecordList = new ArrayList<>();
        AssertionsForClassTypes.assertThatThrownBy(() -> kinesisService.selectRandomRecord(fakeRecordList))
                               .isExactlyInstanceOf(IllegalArgumentException.class)
                               .hasMessageContaining("Records list can not be empty.");

        // Test list with records
        fakeRecordList.add(Record.builder().build());
        fakeRecordList.add(Record.builder().build());
        fakeRecordList.add(Record.builder().build());
        Record record = kinesisService.selectRandomRecord(fakeRecordList);

        // Test a record returns
        assertNotNull(record);
    }

    @Test
    public void testRetrieveRecords() throws IOException {

        Shard shard = Shard.builder().shardId("shardId-1234").build();
        when(kinesisClient.listShards(isA(ListShardsRequest.class)))
                .thenReturn(ListShardsResponse.builder().shards(shard).build());

        when(kinesisClient.getShardIterator(isA(GetShardIteratorRequest.class)))
                .thenReturn(GetShardIteratorResponse.builder().shardIterator("shardIterator").build());

        final Record record = Record.builder().data(SdkBytes.fromByteArray(buildCloudWatchRawPayload())).build();
        GetRecordsResponse recordsResponse = GetRecordsResponse.builder().records(record).millisBehindLatest(10000L).build();
        when(kinesisClient.getRecords(isA(GetRecordsRequest.class)))
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse)
                .thenReturn(recordsResponse);

        List<Record> fakeRecordsList = kinesisService.retrieveRecords("kinesisStream", kinesisClient);
        assertEquals(fakeRecordsList.size(), 10);
    }
}