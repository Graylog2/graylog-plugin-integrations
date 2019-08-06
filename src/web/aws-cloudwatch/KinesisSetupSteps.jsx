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

  const [ stepCreateStream, setStepCreateStream ] = useState({
    label: "Creating Stream",
    request: createStream,
    inProgress: false,
    success: false
  });

  const [ stepCreatePolicy, setStepCreatePolicy ] = useState({
    label: "Creating Policy",
    request: createStream,
    inProgress: false,
    success: false
  });

  const [ stepCreateSubscription, setStepCreateSubscription ] = useState({
    label: "Creating Subscription",
    request: createStream,
    inProgress: false,
    success: false
  });

  useEffect(() => {

    // Add initial delay for easier testing of progress.
    // TODO: Remove
    setTimeout(performAutoSetup, 2000);

  }, []); // [] causes useEffect to only be called once.

  async function performAutoSetup() {

    console.log('Creating stream');
    await createStream();
    console.log('Creating policy');
    await createPolicy();
    console.log('Creating subscription');
    await createSubscription();

    console.log("Done!");
  }

  async function createStream() {
    const url = URLUtils.qualifyUrl(ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_STREAM);
    let promise = await fetch('POST', url,
      {
        aws_access_key_id: key,
        aws_secret_access_key: secret,
        region: 'us-east-1',
        stream_name: 'test-stream',
      });

    // Update progress state
    let updated = { ...stepCreateStream };
    updated.success = true;
    updated.inProgress = true;
    setStepCreateStream(updated);

    return promise;
  }

  async function createPolicy() {
    const url = URLUtils.qualifyUrl(ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_SUBSCRIPTION_POLICY);
    let promise = await fetch('POST', url,
      {
        aws_access_key_id: key,
        aws_secret_access_key: secret,
        region: 'us-east-1',
        role_name: 'role-name',
        stream_name: 'test-stream',
        stream_arn: 'test-stream-arn',
      });

    // Update progress state
    let updated = { ...stepCreatePolicy };
    updated.inProgress = true;
    updated.success = true;
    setStepCreatePolicy(updated);

    return promise;
  }

  async function createSubscription() {
    const url = URLUtils.qualifyUrl(ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_SUBSCRIPTION);
    let promise = await fetch('POST', url,
      {
        aws_access_key_id: key,
        aws_secret_access_key: secret,
        region: 'us-east-1',
        log_group_name: 'log-group',
        filter_name: 'filter-name',
        filter_pattern: 'pattern',
        destination_stream_arn: 'stream-arn',
        role_arn: 'role-arn',
      },);

    // Update progress state
    let updated = { ...stepCreateSubscription };
    updated.inProgress = true;
    updated.success = true;
    setStepCreateSubscription(updated);

    return promise
  }

  return (
    <>
      <h2>Auto-setup</h2><br/>
      <KinesisSetupStep label={stepCreateStream.label} inProgress={stepCreateStream.inProgress}
                        success={stepCreateStream.success}/>
      <KinesisSetupStep label={stepCreatePolicy.label} inProgress={stepCreatePolicy.inProgress}
                        success={stepCreatePolicy.success}/>
      <KinesisSetupStep label={stepCreateSubscription.label} inProgress={stepCreateSubscription.inProgress}
                        success={stepCreateSubscription.success}/>
    </>
  )
};

export default KinesisSetupSteps;
