const FIELDS = [
  /*
  Available options
  {
    id: Reference used in API transaction [required]
    label: Text that renders in UI
    defaultValue: Value that will render as default
    invalidMessage: Custom error message if field is invalid
  }
  */
  /* Default Advanced Values */
  {
    id: 'awsCloudWatchGlobalInput',
    defaultValue: '',
  },
  {
    id: 'awsCloudWatchAssumeARN',
    defaultValue: '',
  },
  {
    id: 'awsCloudWatchBatchSize',
    defaultValue: '10000',
  },
  {
    id: 'awsCloudWatchThrottleEnabled',
    defaultValue: '',
  },
  {
    id: 'awsCloudWatchThrottleWait',
    defaultValue: '1000',
  },
  // Authorize
  {
    id: 'awsCloudWatchName',
    invalidMessage: 'Please provide a name for your new integration.',
    value: 'Name',
  },
  {
    id: 'awsCloudWatchDescription',
    value: 'Description',
  },
  {
    id: 'awsCloudWatchAwsKey',
    invalidMessage: 'Your AWS Key will be 20-character long, alphanumeric string that starts with the letters "AK".',
    value: 'AKQQQQQQQQQQQQQQQQQQ',
  },
  {
    id: 'awsCloudWatchAwsSecret',
    invalidMessage: 'Your AWS Secret will be a 40-character long, base-64 encoded string.',
    value: 'X3lvdXJfYXdzX3NlY3JldF8wMDAwMDAwMDAwMA==',
  },
  {
    id: 'awsCloudWatchAwsRegion',
    value: 'us-east-2',
  },
  // {
  //   id: 'awsCloudWatchKinesisStream',
  // },
  // {
  //   id: 'awsCloudWatchAwsGroupName',
  // },
];

export default FIELDS;
