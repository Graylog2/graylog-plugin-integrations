import React, { useContext, useEffect } from 'react';
import KinesisSetupStep from "./KinesisSetupStep";
import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';
import { ApiRoutes } from "../common/Routes";
import { FormDataContext } from "./context/FormData";
import { awsAuth } from "./context/default_settings";

const KinesisSetupSteps = ({}) => {

  const { formData } = useContext(FormDataContext);
  const { key, secret } = awsAuth(formData);

  useEffect(() => {

    performAutoSetup();
  }, []); // [] causes useEffect to only be called once.

  async function performAutoSetup() {

    console.log("Creating stream");
    await createStream();

    console.log("Creating policy");
    await createPolicy();

    console.log("Creating subscription");
    await createSubscription();

    console.log("Done!");
  }

  async function createStream() {
    const url = URLUtils.qualifyUrl(ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_STREAM);
    return await fetch('POST', url,
      {
        aws_access_key_id: key,
        aws_secret_access_key: secret,
        region: 'us-east-1',
        stream_name: 'test-stream',
      });
  }

  async function createPolicy() {
    const url = URLUtils.qualifyUrl(ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_SUBSCRIPTION_POLICY);
    return await fetch('POST', url,
      {
        aws_access_key_id: key,
        aws_secret_access_key: secret,
        region: 'us-east-1',
        role_name: 'role-name',
        stream_name: 'test-stream',
        stream_arn: 'test-stream-arn',
      });
  }

  async function createSubscription() {
    const url = URLUtils.qualifyUrl(ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_SUBSCRIPTION);
    return await fetch('POST', url,
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
  }

  return (
    <>
      <KinesisSetupStep label={"Step 1: Create Stream"} inProgress={false} success={true}/>
      <KinesisSetupStep label={"Step 2: Create Policy"} inProgress={false} success={true}/>
      <KinesisSetupStep label={"Step 3: Create Subscription"} inProgress={true} success={false}/>
    </>
  );
};

export default KinesisSetupSteps;
