import React, { useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { Alert, Col, Row } from 'react-bootstrap';

import ValidatedInput from '../common/ValidatedInput';
import FormWrap from '../common/FormWrap';
import useFetch from '../common/hooks/useFetch';
import { ApiRoutes } from '../common/Routes';
import { renderOptions } from '../common/Options';
import formValidation from '../utils/formValidation';
import { FormDataContext } from './context/FormData';
import { ApiContext } from './context/Api';
import KinesisSetupSteps from "./KinesisSetupSteps";

const KinesisSetup = ({ onChange, onSubmit, toggleSetup }) => {
  const { availableGroups, setGroups } = useContext(ApiContext);
  const { formData } = useContext(FormDataContext);
  const [ formError, setFormError ] = useState(null);
  const [ disabledGroups, setDisabledGroups ] = useState(false);
  const [ groupNamesStatus, setGroupNamesUrl ] = useFetch(
    ApiRoutes.INTEGRATIONS.AWS.CLOUDWATCH.GROUPS,
    (response) => {
      setGroups(response);
    },
    'POST',
    { region: formData.awsCloudWatchAwsRegion.value },
  );

  const { setStreams } = useContext(ApiContext);
  const [ fetchStreamsStatus, setStreamsFetch ] = useFetch(
    null,
    (response) => {
      setStreams(response);
      toggleSetup();
    },
    'POST',
    { region: formData.awsCloudWatchAwsRegion ? formData.awsCloudWatchAwsRegion.value : '' },
  );

  useEffect(() => {
    setStreamsFetch(null);
    if (groupNamesStatus.error) {
      setGroupNamesUrl(null);

      const noGroups = /No CloudWatch log groups/g;
      if (groupNamesStatus.error.match(noGroups)) {
        setFormError({
                       full_message: groupNamesStatus.error,
                       nice_message: <span>We&apos;re unable to find any groups in your chosen region. Please try choosing a different region, or follow this <a
                         href="/">CloudWatch documentation</a> to begin setting up your AWS CloudWatch account.</span>,
                     });
        setDisabledGroups(true);
      } else {
        setFormError({
                       full_message: groupNamesStatus.error,
                     });
      }
    }

    return () => {
      setGroups({ log_groups: [] });
    };
  }, [ groupNamesStatus.error ]);

  const handleSubmit = () => {
    setDisplaySetupSteps(true);
  };

  const handleContinue = () => {
    setStreamsFetch(ApiRoutes.INTEGRATIONS.AWS.KINESIS.STREAMS);
  };

  const [ displaySetupSteps, setDisplaySetupSteps ] = useState(false);
  const [ setupComplete, toggleSetupComplete ] = useState(false);
  const [ agreedToAWSResourceCreation, setAgreedToAWSResourceCreation ] = useState(false);

  if (!displaySetupSteps) {
    return (
      <Row>
        <Col md={8}>
          <FormWrap onSubmit={handleSubmit}
                    buttonContent="Begin Automated Setup"
                    disabled={formValidation.isFormValid([
                                                           'awsCloudWatchKinesisStream',
                                                           'awsCloudWatchAwsGroupName',
                                                         ], formData)}
                    loading={groupNamesStatus.loading}
                    error={formError}
                    title="Setup Kinesis Automatically"
                    description="">


            <p>Complete the fields below and Graylog will perform the automated Kinesis setup, which performs the
              following operations within your AWS account.
              See <a target={"_blank"}
                     href={"https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/SubscriptionFilters.html"}>Using
                CloudWatch Logs Subscription Filters</a> in the AWS documentation for more information.</p>

            <ol>
              <li>Create a new Kinesis Stream with the specified name.</li>
              <li>Create the IAM role/policy needed to subscribe the Kinesis stream to the CloudWatch Log Group.</li>
              <li>Subscribe the new Kinesis Stream to the Log Group.</li>
            </ol>

            <ValidatedInput id="awsCloudWatchKinesisStream"
                            type="text"
                            label="Kinesis Stream Name"
                            placeholder="Create Stream Name"
                            onChange={onChange}
                            fieldData={formData.awsCloudWatchKinesisStream}
                            required/>

            <ValidatedInput id="awsCloudWatchAwsGroupName"
                            type="select"
                            fieldData={formData.awsCloudWatchAwsGroupName}
                            onChange={onChange}
                            label="CloudWatch Group Name"
                            required
                            disabled={groupNamesStatus.loading || disabledGroups}>

              {renderOptions(availableGroups, 'Choose CloudWatch Group', groupNamesStatus.loading)}
            </ValidatedInput>

            {toggleSetup
            && <button onClick={toggleSetup} type="button" className="btn btn-primary">Back</button>}
            &nbsp;&nbsp;
          </FormWrap>
        </Col>}
      </Row>
    );
  } else {
    return (
      <>
        <Row>
          <Col md={8}>
            <h2>Kinesis Auto Setup</h2>
            <br/>
            <p>This auto setup will create the following AWS resources. Click below to acknowledge that you understand
              that these resources will be created and that you are solely responsible for any associated AWS fees
              incurred
              from them. Note that all resources must be manually deleted by you if they are not needed. </p>

            <ol>
              <li>Create a Kinesis Stream with [1] shard.</li>
              <li>Create an IAM Role and Policy to allow the specified CloudWatch group
                [{formData.awsCloudWatchAwsGroupName.value}]
                to publish log messages to the Kinesis stream [{formData.awsCloudWatchKinesisStream.value}]
              </li>
              <li>Create a CloudWatch Subscription, which publishes log messages to the Kinesis stream.</li>
            </ol>

          </Col>}
        </Row>
        <Row>
          <Col md={8}>
            <button onClick={() => ( setAgreedToAWSResourceCreation(true) )} disabled={agreedToAWSResourceCreation}
                    type="button" className="btn btn-success">
              I Agree! Create these AWS resources now.
            </button>
            <br/>
            <br/>
            <br/>
            {agreedToAWSResourceCreation ?
              <>
                <p>Auto-setup is now executing. Please wait...</p>
                <KinesisSetupSteps toggleSetupInProgress={() => {
                  toggleSetupComplete(true)
                }}/>
              </>
              : ""}
          </Col>}
        </Row>
        {setupComplete ? <Row>
          <Col md={8}>
            <Alert key={"delayedLogs"} variant={"warning"}>
              It may take up to ten minutes for the first messages to arrive in the Kinesis Stream.{' '}
              The Kinesis Health Check in the following step will not complete successfully until messages are present
              in
              the stream{' '}
              Please see the official <a
              href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/Subscriptions.html">CloudWatch
              Subscriptions</a> documentation for more information.
            </Alert>
          </Col>
        </Row> : ""}

        <br/>

        <Row>
          <Col md={8}>
            <button onClick={toggleSetup} type="button" className="btn btn-primary"
                    disabled={setupComplete}>Cancel</button>
            &nbsp;
            <button onClick={handleContinue} type="button" className="btn btn-primary"
                    disabled={!setupComplete}>Continue Setup
            </button>
          </Col>}
        </Row>
      </>
    );
  }
};

KinesisSetup.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  toggleSetup: PropTypes.func,
};

KinesisSetup.defaultProps = {
  toggleSetup: null,
};

export default KinesisSetup;
