import React, { useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { Alert, Panel } from 'react-bootstrap';

import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

import { ApiRoutes } from '../common/Routes';

import { FormDataContext } from './context/FormData';
import { awsAuth } from './context/default_settings';
import KinesisSetupStep from './KinesisSetupStep';

const parseError = (error) => {
  // TODO: Code duplicated from useFetch.
  const fullError = error.additional && error.additional.body && error.additional.body.message;
  return fullError || error.message;
};

const KinesisSetupSteps = ({ onSubmit }) => {
  const { formData } = useContext(FormDataContext);
  const { key, secret } = awsAuth(formData);
  const { awsCloudWatchAwsRegion } = formData;

  function pendingState(message) {
    return {
      type: 'pending',
      additional: message,
    };
  }

  function successState(result) {
    return {
      type: 'success',
      additional: result,
    };
  }

  function errorState(message) {
    return {
      type: 'error',
      additional: message,
    };
  }

  function streamRequest(streamName) {
    return {
      aws_access_key_id: key,
      aws_secret_access_key: secret,
      region: awsCloudWatchAwsRegion.value,
      stream_name: streamName,
    };
  }

  function policyRequest(streamName, streamArn) {
    return {
      aws_access_key_id: key,
      aws_secret_access_key: secret,
      region: awsCloudWatchAwsRegion.value,
      stream_name: streamName,
      stream_arn: streamArn,
    };
  }

  function subscriptionRequest(logGroupName, streamArn, roleArn) {
    return {
      aws_access_key_id: key,
      aws_secret_access_key: secret,
      region: awsCloudWatchAwsRegion.value,
      log_group_name: logGroupName,
      filter_name: 'filter-name', // TODO: Use unique filter name
      filter_pattern: '',
      destination_stream_arn: streamArn,
      role_arn: roleArn,
    };
  }

  // State for each step must be maintained separately in order for the UI to be correctly updated.
  const [success, setSuccess] = useState(false);
  const [streamStep, setStreamStep] = useState({
    label: 'Create Kinesis Stream',
    route: ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_STREAM,
    state: pendingState('Creating stream...'),
  });

  const [policyStep, setPolicyStep] = useState({
    label: 'Create Subscription Policy',
    route: ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_SUBSCRIPTION_POLICY,
    state: pendingState(''),
  });

  const [subscriptionStep, setSubscriptionStep] = useState({
    label: 'Create Subscription',
    route: ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_SUBSCRIPTION,
    state: pendingState(''),
  });

  useEffect(() => {
    async function autoSetup() {
      async function executeStep(step, setStep, request) {
        const url = URLUtils.qualifyUrl(step.route);
        let response;
        try {
          response = await fetch('POST', url, request);
        } catch (e) {
          // eslint-disable-next-line
          console.log('Setup request error:', e);
          const error = errorState(parseError(e));
          setStep({ ...step, state: error });
          throw error;
        }

        // Copy step object and set state field.
        setStep({ ...step, state: successState(response.result) });
        return response;
      }

      // Flow control for auto-setup steps.

      // Create Stream
      let response = await executeStep(streamStep, setStreamStep, streamRequest(formData.awsCloudWatchKinesisStream.value)); // TODO: Pull from input field.

      const streamArn = response.stream_arn;
      response = await executeStep(policyStep, setPolicyStep, policyRequest(response.stream_name,
        streamArn));

      await executeStep(subscriptionStep, setSubscriptionStep, subscriptionRequest(formData.awsCloudWatchAwsGroupName.value,
        streamArn,
        response.role_arn));

      setSuccess(true);
    }

    // TODO: Display success message.

    // TODO: Add navigation and the ability to interrupt?

    autoSetup();
  }, []);

  return (
    <Panel header={<p>Executing Auto-Setup</p>}>
      <KinesisSetupStep step={streamStep} />
      <KinesisSetupStep step={policyStep} />
      <KinesisSetupStep step={subscriptionStep} />

      {success && (
      <>
        <Alert key="delayedLogs" variant="warning">
          It may take up to ten minutes for the first messages to arrive in the Kinesis Stream. The Kinesis Health Check in the following step will not complete successfully until messages are present in the stream. Please see the official <a href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/Subscriptions.html" target="_blank" rel="noopener noreferrer">CloudWatch Subscriptions</a> documentation for more information.
        </Alert>

        <button onClick={onSubmit}
                type="button"
                className="btn btn-primary">Continue Setup
        </button>
      </>
      )}
    </Panel>
  );
};

KinesisSetupSteps.propTypes = {
  onSubmit: PropTypes.func.isRequired,
};

export default KinesisSetupSteps;
