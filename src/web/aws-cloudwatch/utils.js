const FIELDS = [
  /*
  Available options
  {
    id: Reference used in API transaction [required]
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
  {
    id: 'awsCloudWatchName',
    value: 'Name',
  },
  {
    id: 'awsCloudWatchDescription',
    value: 'Description',
  },
  {
    id: 'awsCloudWatchAwsKey',
    value: 'AKIAWV25I6RLR6LN7Q32',
    // value: 'AKQQQQQQQQQQQQQQQQQQ',
  },
  {
    id: 'awsCloudWatchAwsSecret',
    value: 'LrSKpYNlSTaEp/vEOCpF+Rukx/U2YKALCKZ4PfgI',
    // value: 'X3lvdXJfYXdzX3NlY3JldF8wMDAwMDAwMDAwMA==',
  },
  {
    id: 'awsCloudWatchAwsRegion',
    value: 'us-east-1',
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
