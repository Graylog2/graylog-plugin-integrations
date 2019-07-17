package org.graylog.integrations.aws.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AssertionsForClassTypes;
import org.graylog.integrations.aws.AWSLogMessage;
import org.graylog.integrations.aws.AWSMessageType;
import org.graylog.integrations.aws.codecs.KinesisCloudWatchFlowLogCodec;
import org.graylog.integrations.aws.codecs.KinesisRawLogCodec;
import org.graylog.integrations.aws.resources.requests.KinesisHealthCheckRequest;
import org.graylog.integrations.aws.resources.requests.KinesisNewStreamRequest;
import org.graylog.integrations.aws.resources.responses.KinesisHealthCheckResponse;
import org.graylog.integrations.aws.resources.responses.StreamsResponse;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.codecs.Codec;
import org.graylog2.shared.bindings.providers.ObjectMapperProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.CreateStreamResponse;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

public class KinesisServiceTest {

    private static final String TEST_STREAM_1 = "test-stream-1";
    private static final String TEST_STREAM_2 = "test-stream-2";
    private static final String[] TWO_TEST_STREAMS = {TEST_STREAM_1, TEST_STREAM_2};
    private static final String TEST_REGION = Region.EU_WEST_1.id();
    private static final int SHARD_COUNT = 1;

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

        ObjectMapper objectMapper = new ObjectMapperProvider().get();

        // Create an AWS client with a mock KinesisClientBuilder
        availableCodecs = new HashMap<>();

        // Prepare test codecs. These have to be manually instantiated for the test context.
        availableCodecs.put(KinesisRawLogCodec.NAME, new KinesisRawLogCodec.Factory() {
            @Override
            public KinesisRawLogCodec create(Configuration configuration) {
                return new KinesisRawLogCodec(configuration, objectMapper);
            }

            @Override
            public KinesisRawLogCodec.Config getConfig() {
                return null;
            }

            @Override
            public Codec.Descriptor getDescriptor() {
                return null;
            }
        });

        availableCodecs.put(KinesisCloudWatchFlowLogCodec.NAME, new KinesisCloudWatchFlowLogCodec.Factory() {
            @Override
            public KinesisCloudWatchFlowLogCodec create(Configuration configuration) {
                return new KinesisCloudWatchFlowLogCodec(configuration, objectMapper);
            }

            @Override
            public KinesisCloudWatchFlowLogCodec.Config getConfig() {
                return null;
            }

            @Override
            public Codec.Descriptor getDescriptor() {
                return null;
            }
        });

        kinesisService = new KinesisService(kinesisClientBuilder, objectMapper, availableCodecs);
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
    public void healthCheckCloudWatchFlowLog() throws ExecutionException, IOException {

        // The recordArrivalTime does not matter here, since the CloudWatch timestamp is used for the message instead.
        KinesisHealthCheckResponse response = executeHealthCheckTest(buildCloudWatchFlowLogPayload(),
                                                                     Instant.now());
        assertEquals(AWSMessageType.KINESIS_FLOW_LOGS, response.inputType());
        Map<String, Object> fields = response.messageFields();
        assertEquals(new DateTime("2019-06-05T12:35:44.000Z", DateTimeZone.UTC), fields.get("timestamp"));
        assertEquals(21, fields.size());
        assertEquals(6, fields.get("protocol_number"));
        assertEquals("TCP", fields.get("protocol"));
        assertEquals(1L, fields.get("packets"));
        assertEquals("172.1.1.2", fields.get("dst_addr"));
    }

    @Test
    public void healthCheckCloudWatchRaw() throws ExecutionException, IOException {

        // The recordArrivalTime does not matter here, since the CloudWatch timestamp is used for the message instead.
        KinesisHealthCheckResponse response = executeHealthCheckTest(buildCloudWatchRawPayload(), Instant.now());
        assertEquals(AWSMessageType.KINESIS_RAW, response.inputType());
        Map<String, Object> fields = response.messageFields();
        assertEquals(new DateTime("2019-06-05T12:35:44.000Z", DateTimeZone.UTC), fields.get("timestamp"));
        assertEquals(7, fields.size());
    }

    @Test
    public void healthCheckRawKinesisLog() throws ExecutionException, IOException {

        // Use a specific log arrival time to ensure correct timezone is set on resulting message.
        // 2000-01-01T01:01:01Z
        Instant logArrivalTime = Instant.ofEpochMilli(new DateTime(2000, 1, 1, 1, 1, 1, DateTimeZone.UTC).getMillis());
        KinesisHealthCheckResponse response = executeHealthCheckTest("This is a test raw log".getBytes(), logArrivalTime);
        assertEquals(AWSMessageType.KINESIS_RAW, response.inputType());
        Map<String, Object> fields = response.messageFields();
        assertEquals(new DateTime("2000-01-01T01:01:01.000Z", DateTimeZone.UTC), fields.get("timestamp"));
        assertEquals(5, fields.size());
    }

    private KinesisHealthCheckResponse executeHealthCheckTest(byte[] payloadData, Instant recordArrivalTime) throws IOException, ExecutionException {

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

        final Record record = Record.builder()
                                    .approximateArrivalTimestamp(recordArrivalTime)
                                    .data(SdkBytes.fromByteArray(payloadData))
                                    .build();
        when(kinesisClient.getRecords(isA(GetRecordsRequest.class)))
                .thenReturn(GetRecordsResponse.builder().records(record).millisBehindLatest(10000L).build())
                .thenReturn(GetRecordsResponse.builder().records(record).millisBehindLatest(0L).build());

        KinesisHealthCheckRequest request = KinesisHealthCheckRequest.create(Region.EU_WEST_1.id(),
                                                                             "key", "secret", TEST_STREAM_1);
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
                                   "      \"timestamp\": 1559738144000,\n" + // Equal to 2019-06-05T12:35:44.000Z
                                   "      \"message\": \"Just a raw message\"\n" +
                                   "    },\n" +
                                   "    {\n" +
                                   "      \"id\": \"3423\",\n" +
                                   "      \"timestamp\": 1559738144000,\n" + // Equal to 2019-06-05T12:35:44.000Z
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
        AssertionsForClassTypes.assertThatThrownBy(() -> kinesisService.getKinesisStreamNames(TEST_REGION, "", ""))
                               .isExactlyInstanceOf(IllegalArgumentException.class)
                               .hasMessageContaining("An AWS access key is required");
        AssertionsForClassTypes.assertThatThrownBy(() -> kinesisService.getKinesisStreamNames(TEST_REGION, "dsfadsdf", ""))
                               .isExactlyInstanceOf(IllegalArgumentException.class)
                               .hasMessageContaining("An AWS secret key is required");
        AssertionsForClassTypes.assertThatThrownBy(() -> kinesisService.getKinesisStreamNames(TEST_REGION, "", "dsfadsdf"))
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


        StreamsResponse streamsResponse = kinesisService.getKinesisStreamNames(TEST_REGION, "accessKey", "secretKey");
        assertEquals(2, streamsResponse.total());
        assertEquals(2, streamsResponse.streams().size());

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

        streamsResponse = kinesisService.getKinesisStreamNames(TEST_REGION, "accessKey", "secretKey");

        // There should be 4 total streams (two from each page).
        assertEquals(4, streamsResponse.total());
        assertEquals(4, streamsResponse.streams().size());
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

        final Record record = Record.builder()
                                    .approximateArrivalTimestamp(Instant.now())
                                    .data(SdkBytes.fromByteArray(buildCloudWatchRawPayload()))
                                    .build();
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

    @Test
    public void testCreateNewKinesisStream() {

        // These three lines mock the KinesisClient. Must be repeated for every test.
        when(kinesisClientBuilder.region(isA(Region.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.credentialsProvider(isA(AwsCredentialsProvider.class))).thenReturn(kinesisClientBuilder);
        when(kinesisClientBuilder.build()).thenReturn(kinesisClient);

        // Mock out specific KinesisNewStreamRequest to return a response.
        when(kinesisClient.createStream(isA(CreateStreamRequest.class))).thenReturn(CreateStreamResponse.builder().build());

        KinesisNewStreamRequest kinesisNewStreamRequest = KinesisNewStreamRequest.create(TEST_REGION,
                                                                                         "accessKey", "secretKey",
                                                                                         TEST_STREAM_1);
        kinesisService.createNewKinesisStream(kinesisNewStreamRequest);

        // Check the values are whats expected.
        assertEquals(kinesisNewStreamRequest.region(), TEST_REGION);
        assertEquals(kinesisNewStreamRequest.awsAccessKeyId(), "accessKey");
        assertEquals(kinesisNewStreamRequest.awsSecretAccessKey(), "secretKey");
        assertEquals(kinesisNewStreamRequest.streamName(), TEST_STREAM_1);
        assertEquals(SHARD_COUNT, 1);
    }
}