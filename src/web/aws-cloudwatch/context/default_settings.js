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
    value: 'eu-west-1',
  },
  /* End Test Settings */
};

export const awsAuth = ({ awsCloudWatchAwsKey, awsCloudWatchAwsSecret }) => {
  /*
    For development, create a sibling file named `aws.js` in the current directory.
    ```module.exports = { key: 'YOUR_REAL_KEY', secret: 'YOUR_REAL_SECRET' };```
    This file is already set in .gitignore so it won't be commited
  */

  let auth = { key: awsCloudWatchAwsKey, secret: awsCloudWatchAwsSecret };

  try {
    // eslint-disable-next-line global-require
    auth = require('./aws');
  } catch (e) {
    return auth;
  }

  return auth;
};

export default DEFAULT_SETTINGS;
