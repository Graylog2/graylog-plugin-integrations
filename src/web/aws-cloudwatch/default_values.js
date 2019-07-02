const DEFAULT_VALUES = [
  /*
  Available options
  {
    id: Reference used in API transaction [required]
    defaultValue: Value that will render as default
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
];

export default DEFAULT_VALUES;
