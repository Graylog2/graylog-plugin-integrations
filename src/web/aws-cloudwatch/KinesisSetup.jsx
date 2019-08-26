import React, { useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';

import ValidatedInput from '../common/ValidatedInput';
import FormWrap from '../common/FormWrap';
import { ApiRoutes } from '../common/Routes';
import { renderOptions } from '../common/Options';
import useFetch from '../common/hooks/useFetch';

import formValidation from '../utils/formValidation';

import { FormDataContext } from './context/FormData';
import { ApiContext } from './context/Api';

import SetupModal from './auto-setup-steps/SetupModal';

<<<<<<< HEAD
const KinesisSetup = ({ onChange, onSubmit, toggleSetup }) => {
=======
const KinesisSetup = ({ onChange, /* onSubmit, */ toggleSetup }) => {
>>>>>>> master
  const { availableGroups, setGroups } = useContext(ApiContext);
  const { formData } = useContext(FormDataContext);
  const [formError, setFormError] = useState(null);
  const [disabledForm, setDisabledForm] = useState(false);
  const [disabledGroups, setDisabledGroups] = useState(false);
  const [showTOS, setShowTOS] = useState(false);
  const [groupNamesStatus, setGroupNamesUrl] = useFetch(
    ApiRoutes.INTEGRATIONS.AWS.CLOUDWATCH.GROUPS,
    (response) => {
      setGroups(response);
    },
    'POST',
    { region: formData.awsCloudWatchAwsRegion.value },
  );

<<<<<<< HEAD
  useEffect(() => {
=======
  const { setStreams } = useContext(ApiContext);
  const [fetchStreamsStatus, setStreamsFetch] = useFetch(
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
>>>>>>> master
    if (groupNamesStatus.error) {
      setGroupNamesUrl(null);

      const noGroups = /No CloudWatch log groups/g;
      if (groupNamesStatus.error.match(noGroups)) {
        // TODO: Use real CloudWatch documentation link
        setFormError({
          full_message: groupNamesStatus.error,
          nice_message: <span>We&apos;re unable to find any groups in your chosen region. Please try choosing a different region, or follow this <a href="/">CloudWatch documentation</a> to begin setting up your AWS CloudWatch account.</span>,
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
  }, [groupNamesStatus.error]);

  const handleAgreeSubmit = () => {
<<<<<<< HEAD
    onSubmit();
=======
    setStreamsFetch(ApiRoutes.INTEGRATIONS.AWS.KINESIS.STREAMS);
>>>>>>> master
  };

  const handleFormSubmit = () => {
    setDisabledForm(true);
    setShowTOS(true);
  };

  const handleAgreeCancel = () => {
    setDisabledForm(false);
    setShowTOS(false);
  };

  return (
    <FormWrap onSubmit={handleFormSubmit}
              buttonContent="Begin Automated Setup"
              disabled={formValidation.isFormValid([
                'awsCloudWatchKinesisStream',
                'awsCloudWatchAwsGroupName',
              ], formData) || disabledForm}
<<<<<<< HEAD
              loading={groupNamesStatus.loading}
=======
              loading={groupNamesStatus.loading || fetchStreamsStatus.loading}
>>>>>>> master
              error={formError}
              title="Setup Kinesis Automatically"
              description="">

<<<<<<< HEAD
      <p>
        Complete the fields below and Graylog will perform the automated Kinesis setup, which performs the following operations within your AWS account. See <a target="_blank" rel="noopener noreferrer" href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/SubscriptionFilters.html">Using CloudWatch Logs Subscription Filters</a> in the AWS documentation for more information.
=======
      <p>Complete the fields below and Graylog will perform the automated Kinesis setup, which performs the
              following operations within your AWS account.
              See <a target="_blank" rel="noopener noreferrer" href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/SubscriptionFilters.html">Using CloudWatch Logs Subscription Filters</a> in the AWS documentation for more information.
>>>>>>> master
      </p>

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
                      disabled={disabledForm}
<<<<<<< HEAD
=======
                      pattern="[a-zA-Z0-9_.-]{1,128}$"
                      help="1-128 alphanumeric characters and special characters underscore (_), period (.), and hyphen (-)."
>>>>>>> master
                      required />

      <ValidatedInput id="awsCloudWatchAwsGroupName"
                      type="select"
                      fieldData={formData.awsCloudWatchAwsGroupName}
                      onChange={onChange}
                      label="CloudWatch Group Name"
                      required
                      disabled={groupNamesStatus.loading || disabledGroups || disabledForm}>

        {renderOptions(availableGroups, 'Choose CloudWatch Group', groupNamesStatus.loading)}
      </ValidatedInput>

      {toggleSetup
        && (
        <BackButton onClick={toggleSetup}
                    type="button"
<<<<<<< HEAD
                    className="btn btn-default"
=======
                    className="btn btn-primary"
>>>>>>> master
                    disabled={disabledForm}>
          Back to Stream Selection
        </BackButton>
        )}

      {showTOS && (
      <SetupModal onSubmit={handleAgreeSubmit}
                  onCancel={handleAgreeCancel}
                  groupName={formData.awsCloudWatchAwsGroupName.value}
                  streamName={formData.awsCloudWatchKinesisStream.value} />
      )}
    </FormWrap>
  );
};

KinesisSetup.propTypes = {
<<<<<<< HEAD
  onSubmit: PropTypes.func.isRequired,
=======
  // onSubmit: PropTypes.func.isRequired,
>>>>>>> master
  onChange: PropTypes.func.isRequired,
  toggleSetup: PropTypes.func,
};

KinesisSetup.defaultProps = {
  toggleSetup: null,
};

const BackButton = styled.button`
  margin-right: 9px;
`;

export default KinesisSetup;
