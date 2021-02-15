/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

import { toGenericInputCreateRequest } from './formDataAdapter';
import { AWS_AUTH_TYPES, DEFAULT_KINESIS_LOG_TYPE } from './constants';

describe('formDataAdapter', () => {
  const testGenericInputCreateRequest = (formData) => {
    let awsAccessKey = 'key';
    let awsAccessSecret = 'secret';

    if (formData.awsAuthenticationType?.value === AWS_AUTH_TYPES.keysecret) {
      awsAccessKey = 'awsCloudWatchAwsKey';
      awsAccessSecret = 'awsCloudWatchAwsSecret';
    }

    // Mapping keys taken from /api/system/inputs/types/org.graylog.integrations.aws.inputs.AWSInput
    const mappings = {
      aws_access_key: awsAccessKey,
      aws_assume_role_arn: 'awsCloudWatchAssumeARN',
      aws_flow_log_prefix: 'awsCloudWatchAddFlowLogPrefix',
      aws_message_type: 'awsCloudWatchKinesisInputType',
      aws_region: 'awsCloudWatchAwsRegion',
      aws_secret_key: awsAccessSecret,
      cloudwatch_endpoint: 'awsEndpointCloudWatch',
      dynamodb_endpoint: 'awsEndpointDynamoDB',
      iam_endpoint: 'awsEndpointIAM',
      kinesis_endpoint: 'awsEndpointKinesis',
      kinesis_record_batch_size: 'awsCloudWatchBatchSize',
      kinesis_stream_name: 'awsCloudWatchKinesisStream',
      throttling_allowed: 'awsCloudWatchThrottleEnabled',
    };

    const request = toGenericInputCreateRequest(formData);

    expect(request.type).toBe('org.graylog.integrations.aws.inputs.AWSInput');
    expect(request.title).toEqual(formData.awsCloudWatchName.value);
    expect(request.global).toEqual(formData.awsCloudWatchGlobalInput.value);

    const { configuration } = request;

    expect(Object.keys(configuration).sort()).toEqual(Object.keys(mappings).sort());

    Object.entries(configuration).forEach(([key, value]) => {
      const formDataValue = (mappings[key] === 'key' || mappings[key] === 'secret'
        ? formData[mappings[key]]
        : formData[mappings[key]].value);

      expect(value).toEqual(formDataValue);
    });

    return request;
  };

  it('adapts formData into an InputCreateRequest with key & secret', () => {
    testGenericInputCreateRequest({
      awsAuthenticationType: { value: AWS_AUTH_TYPES.keysecret },
      awsCloudWatchAddFlowLogPrefix: { value: true },
      awsCloudWatchAssumeARN: { value: '' },
      awsCloudWatchAwsKey: { value: 'mykey' },
      awsCloudWatchAwsRegion: { value: 'us-east-1' },
      awsCloudWatchBatchSize: { value: 10000 },
      awsEndpointCloudWatch: { value: undefined },
      awsCloudWatchGlobalInput: { value: false },
      awsCloudWatchKinesisInputType: { value: DEFAULT_KINESIS_LOG_TYPE },
      awsCloudWatchKinesisStream: { value: 'my-stream' },
      awsCloudWatchName: { value: 'My Input' },
      awsCloudWatchThrottleEnabled: { value: false },
      awsEndpointDynamoDB: { value: undefined },
      awsEndpointIAM: { value: undefined },
      awsEndpointKinesis: { value: undefined },
      awsCloudWatchAwsSecret: { value: 'mysecret' },
    });
  });

  it('adapts formData into an InputCreateRequest with automatic auth', () => {
    testGenericInputCreateRequest({
      awsAuthenticationType: { value: AWS_AUTH_TYPES.automatic },
      awsCloudWatchAddFlowLogPrefix: { value: true },
      awsCloudWatchAssumeARN: { value: '' },
      awsCloudWatchAwsRegion: { value: 'us-east-1' },
      awsCloudWatchBatchSize: { value: 10000 },
      awsEndpointCloudWatch: { value: undefined },
      awsCloudWatchGlobalInput: { value: false },
      awsCloudWatchKinesisInputType: { value: DEFAULT_KINESIS_LOG_TYPE },
      awsCloudWatchKinesisStream: { value: 'my-stream' },
      awsCloudWatchName: { value: 'My Input' },
      awsCloudWatchThrottleEnabled: { value: false },
      awsEndpointDynamoDB: { value: undefined },
      awsEndpointIAM: { value: undefined },
      awsEndpointKinesis: { value: undefined },
      key: 'mykey',
      secret: 'mysecret',
    });
  });
});
