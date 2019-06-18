const FIELDS = [
  /*
  Available options
  {
    id: Reference used in API transaction [required]
    label: Text that renders in UI
    defaultValue: Value that will render as default
    errorMessage: Custom error message if field is invalid
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
  // {
  //   id: 'awsCloudWatchName',
  //   value: 'Name',
  // },
  // {
  //   id: 'awsCloudWatchDescription',
  //   value: 'Description',
  // },
  {
    id: 'awsCloudWatchAwsKey',
    errorMessage: 'Your AWS Key will be 20-character long, alphanumeric string that starts with the letters "AK".',
    // value: 'AKQQQQQQQQQQQQQQQQQQ',
  },
  {
    id: 'awsCloudWatchAwsSecret',
    errorMessage: 'Your AWS Secret will be a 40-character long, base-64 encoded string.',
    // value: 'X3lvdXJfYXdzX3NlY3JldF8wMDAwMDAwMDAwMA==',
  },
  {
    id: 'awsCloudWatchAwsRegion',
    errorMessage: 'Provide the region your CloudWatch instance is deployed.',
    // value: 'us-east-2',
  },
  // {
  //   id: 'awsCloudWatchKinesisStream',
  // },
  // {
  //   id: 'awsCloudWatchAwsGroupName',
  // },
];

export default FIELDS;
