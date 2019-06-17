// const DEFAULT_SETTINGS = {
//   /* Default Advanced Values */
//   awsCloudWatchGlobalInput: '',
//   awsCloudWatchAssumeARN: '',
//   awsCloudWatchBatchSize: '10000',
//   awsCloudWatchThrottleEnabled: '',
//   awsCloudWatchThrottleWait: '1000',
// };

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
  // authorize: [
  //   {
  //     id: 'awsCloudWatchName',
  //     invalidMessage: 'Please provide a name for your new integration.',
  //     label: 'Name',
  //   },
  //   {
  //     id: 'awsCloudWatchDescription',
  //     label: 'Name',
  //   },
  //   {
  //     id: 'awsCloudWatchAwsKey',
  //     invalidMessage: 'Your AWS Key will be 20-character long, alphanumeric string that starts with the letters "AK".',
  //     label: 'Name',
  //   },
  //   {
  //     id: 'awsCloudWatchAwsSecret',
  //     invalidMessage: 'Your AWS Secret will be a 40-character long, base-64 encoded string.',
  //     label: 'Name',
  //   },
  //   {
  //     id: 'awsCloudWatchAwsRegion',
  //     label: 'Name',
  //   },
  // ],
  // 'kinesis-setup': [
  //   {
  //     id: 'awsCloudWatchKinesisStream',
  //     label: 'Name',
  //   },
  //   {
  //     id: 'awsCloudWatchAwsGroupName',
  //     label: 'Name',
  //   },
  // ],
];

export default FIELDS;
