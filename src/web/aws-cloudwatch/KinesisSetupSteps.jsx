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

  function pending() {
    return {
      type: 'pending',
      additional: 'The set has not begun'
    };
  }

  function success() {
    return {
      type: 'success',
      additional: 'The step was successful'
    };
  }

  const [ steps, setSteps ] = useState({
    Stream: {
      label: "Creating stream",
      route: ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_STREAM,
      request: {
        aws_access_key_id: key,
        aws_secret_access_key: secret,
        region: 'us-east-1',
        stream_name: 'test-stream',
      },
      state: pending()
    },
    Policy: {
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
      state: pending()
    },
    Subscription: {
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
      state: pending()
    }
  });

  const setStep = (id, newValue) => setSteps({ ...steps, [ id ]: newValue });
  const setState = (id, newState) => setStep(id, { ...steps[ id ], state: newState });

  useEffect(() => {

    async function autoSetup() {

      for (let [ id, step ] of Object.entries(steps)) {
        const url = URLUtils.qualifyUrl(step.route);
        await fetch('POST', url, step.request);
        setState(id, { ...step, state: success() });
      }
    }

    autoSetup()
  }, []); // [] causes useEffect to only be called once.

  return (
    Object.values(steps).map((step) => {
      return <KinesisSetupStep key={step.label} label={step.label} state={step.state}/>
    }) );
};

export default KinesisSetupSteps;
