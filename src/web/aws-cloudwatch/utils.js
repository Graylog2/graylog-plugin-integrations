const FIELDS = [
  /*
  Available options
  {
    id: Reference used in API transaction [required]
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
    // value: 'AKQQQQQQQQQQQQQQQQQQ',
  },
  {
    id: 'awsCloudWatchAwsSecret',
    // value: 'X3lvdXJfYXdzX3NlY3JldF8wMDAwMDAwMDAwMA==',
  },
  {
    id: 'awsCloudWatchAwsRegion',
    // value: 'us-east-2',
  },
  // {
  //   id: 'awsCloudWatchKinesisStream',
  // },
  // {
  //   id: 'awsCloudWatchAwsGroupName',
  // },
];

function normalizeFields(fields) {
  const normal = {
    defaultValue: '',
    dirty: false,
    error: false,
    value: '',
  };

  return fields.map((field) => {
    return {
      ...normal,
      ...field,
    };
  });
}

export default normalizeFields(FIELDS);
