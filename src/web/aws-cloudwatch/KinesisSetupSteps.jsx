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

  const [ steps, setSteps ] = useState([ {
    label: "Creating stream",
    request: createStream,
    inProgress: false,
    success: false
  }, {
    label: "Creating policy",
    request: createPolicy,
    inProgress: false,
    success: false
  }, {
    label: "Creating subscription",
    request: createSubscription,
    inProgress: false,
    success: false
  } ]);

  useEffect(() => {

    performAutoSetup()
  }, []); // [] causes useEffect to only be called once.

  async function performAutoSetup() {

    for (const step of this.steps) {
      console.log(step.label);
      await step.request(step);
    }

    console.log("Done!");
  }

  async function createStream(step) {
    const url = URLUtils.qualifyUrl(ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_STREAM);
    let promise = await fetch('POST', url,
      {
        aws_access_key_id: key,
        aws_secret_access_key: secret,
        region: 'us-east-1',
        stream_name: 'test-stream',
      });
    step.label = 'hi';
    step.inProgress = true;
    return promise;
  }

  async function createPolicy(step) {
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
    step.inProgress = true;
    return promise;
  }

  async function createSubscription(step) {
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
    step.inProgress = true;
  }

  return (

    steps.map((item, key) => {
        return <KinesisSetupStep label={item.label} inProgress={item.inProgress} success={item.success}/>
      }
    )
  );
};

export default KinesisSetupSteps;
