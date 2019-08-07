import React, { useContext, useEffect, useState } from 'react';
import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';
import { ApiRoutes } from "../common/Routes";
import { FormDataContext } from "./context/FormData";
import { awsAuth } from "./context/default_settings";
import KinesisSetupStep from "./KinesisSetupStep";

const KinesisSetupSteps = ({}) => {

  const { formData } = useContext(FormDataContext);
  const { key, secret } = awsAuth(formData);

  function pendingState() {
    return {
      type: 'pending',
      additional: 'Waiting...'
    };
  }

  function successState() {
    return {
      type: 'success',
      additional: 'Successfully created stream [dano-stream/arn-01]'
    };
  }

  function errorState(message) {
    return {
      type: 'error',
      additional: message
    };
  }

  // State for each step must be maintained separately in order for the UI to be correctly updated.
  let [ streamStep, setStreamStep ] = useState({
    label: "Creating stream",
    route: ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_STREAM,
    request: {
      aws_access_key_id: key,
      aws_secret_access_key: secret,
      region: 'us-east-1',
      stream_name: 'test-stream',
    },
    state: pendingState()
  });

  let [ policyStep, setPolicyStep ] = useState({
    label: "Creating policy",
    route: ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_SUBSCRIPTION_POLICY,
    request: {
      aws_access_key_id: key,
      aws_secret_access_key: secret,
      region: 'us-east-1',
      role_name: 'role-name',
      stream_name: 'test-stream',
      stream_arn: 'test-stream-arn',
    },
    state: pendingState()
  });

  let [ subscriptionStep, setSubscriptionStep ] = useState({
    label: "Creating subscription",
    route: ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_SUBSCRIPTION,
    request: {
      aws_access_key_id: key,
      aws_secret_access_key: secret,
      region: 'us-east-1',
      log_group_name: 'log-group',
      filter_name: 'filter-name',
      filter_pattern: 'pattern',
      destination_stream_arn: 'stream-arn',
      role_arn: 'role-arn',
    },
    state: pendingState()
  });

  useEffect(() => {

      async function autoSetup() {

        async function executeStep(step, setStep) {
          const url = URLUtils.qualifyUrl(step.route);
          let response;
          try {
            response = await fetch('POST', url, step.request);
          } catch (e) {
            console.log('Setup request error:', e);
            let error = errorState(parseError(e));
            setStep({ ...step, state: error });
            throw error;
          }

          // Copy step object and set state field.
          setStep({ ...step, state: successState() });
          return response;
        }

        await executeStep(streamStep, setStreamStep);
        await executeStep(policyStep, setPolicyStep);
        await executeStep(subscriptionStep, setSubscriptionStep);
      }

      autoSetup()
    }, [] // [] causes useEffect to only be called once.
  );

  return (
    <>
      <KinesisSetupStep step={streamStep}/>
      <KinesisSetupStep step={policyStep}/>
      <KinesisSetupStep step={subscriptionStep}/>
    </> );
};

const parseError = (error) => {
  const fullError = error.additional && error.additional.body && error.additional.body.message;
  return fullError || error.message;
};

export default KinesisSetupSteps;
