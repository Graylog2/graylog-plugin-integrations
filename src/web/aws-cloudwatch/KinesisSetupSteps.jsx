import React, { useContext, useEffect, useState } from 'react';
import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';
import PropTypes from 'prop-types';
import { ApiRoutes } from '../common/Routes';
import { FormDataContext } from './context/FormData';
import { awsAuth } from './context/default_settings';
import KinesisSetupStep from './KinesisSetupStep';

// TODO: Code duplicated from useFetch.
const parseError = (error) => {
  const fullError = error.additional && error.additional.body && error.additional.body.message;
  return fullError || error.message;
};

const KinesisSetupSteps = ({ toggleSetupInProgress }) => {
  const { formData } = useContext(FormDataContext);
  const { key, secret } = awsAuth(formData);

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
      region: 'us-east-1',
      stream_name: streamName,
    };
  }

  function policyRequest(streamName, streamArn) {
    return {
      aws_access_key_id: key,
      aws_secret_access_key: secret,
      region: 'us-east-1',
      stream_name: streamName,
      stream_arn: streamArn,
    };
  }

  function subscriptionRequest(logGroupName, streamArn, roleArn) {
    return {
      aws_access_key_id: key,
      aws_secret_access_key: secret,
      region: 'us-east-1',
      log_group_name: logGroupName,
      filter_name: 'filter-name', // TODO: Use unique filter name
      filter_pattern: '',
      destination_stream_arn: streamArn,
      role_arn: roleArn,
    };
  }

  // State for each step must be maintained separately in order for the UI to be correctly updated.
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
      // setPolicyStep(pendingState('Creating policy...'));
      response = await executeStep(policyStep, setPolicyStep, policyRequest(response.stream_name,
        streamArn));

      await executeStep(subscriptionStep, setSubscriptionStep, subscriptionRequest(formData.awsCloudWatchAwsGroupName.value,
        streamArn,
        response.role_arn));
      toggleSetupInProgress();
    }

    // TODO: Display success message.

    // TODO: Add navigation and the ability to interrupt?

    autoSetup();
  }, []);

  return (
    <>
      <KinesisSetupStep step={streamStep} />
      <KinesisSetupStep step={policyStep} />
      <KinesisSetupStep step={subscriptionStep} />
    </>
  );
};

KinesisSetupSteps.propTypes = {
  toggleSetupInProgress: PropTypes.func.isRequired,
};

export default KinesisSetupSteps;
