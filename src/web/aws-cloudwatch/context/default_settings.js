const DEFAULT_SETTINGS = {
  /*
  fieldId: { // Same ID as supplied to <Input />
    value: '',
    defaultValue: '', // Update StepReview.jsx & relevant step(s) if you need to output
  }
  */

  /* Default Advanced Settings */
  awsCloudWatchBatchSize: {
    defaultValue: '10000',
  },
  awsCloudWatchThrottleWait: {
    defaultValue: '1000',
  },

  /* Test Settings */
  // TODO: Remove these before any official launch, but I'm tired of copy/paste during dev
  awsCloudWatchName: {
    value: 'Name',
  },
  awsCloudWatchDescription: {
    value: 'Description',
  },
  awsCloudWatchAwsKey: {
    value: 'AKQQQQQQQQQQQQQQQQQQ',
  },
  awsCloudWatchAwsSecret: {
    value: 'XfoodLP3YXdzX3NlY3JldF8wMDNOTDAwMDbeMA==',
  },
  awsCloudWatchAwsRegion: {
    value: 'us-east-2',
  },
  /* End Test Settings */
};

export default DEFAULT_SETTINGS;
