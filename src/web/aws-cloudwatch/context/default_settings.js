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
    value: 'ABC',
  },
  awsCloudWatchAwsSecret: {
    value: '123',
  },
  awsCloudWatchAwsRegion: {
    value: 'us-east-2',
  },
  /* End Test Settings */
};

export const awsAuth = () => {
  const auth = { key: 'AKXXXXXXXXXXXXXXXXXX', secret: 'SECRETXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==' };

  /*
    Create a sibling file named `aws.js` in the current directory.
    ```export default () => ({ key: '...', secret: '...' });```
    This file is already set in .gitignore
  */

  // import('./aws')
  //   .then((awsData) => {
  //     return awsData.default();
  //   });

  return Promise.resolve(auth);
};

export default DEFAULT_SETTINGS;
