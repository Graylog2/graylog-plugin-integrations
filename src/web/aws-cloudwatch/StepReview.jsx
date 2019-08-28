import React, { useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';
import { Link } from 'react-router';

import Routes from 'routing/Routes';
import { Input } from 'components/bootstrap';

import { FormDataContext } from './context/FormData';
import { ApiContext } from './context/Api';

import useFetch from '../common/hooks/useFetch';
import FormWrap from '../common/FormWrap';
import { ApiRoutes } from '../common/Routes';
import { DEFAULT_KINESIS_LOG_TYPE, KINESIS_LOG_TYPES } from '../common/constants';

const Default = ({ value }) => {
  return (
    <React.Fragment>
      {value} <small>(default)</small>
    </React.Fragment>
  );
};

Default.propTypes = {
  value: PropTypes.string.isRequired,
};

const StepReview = ({ onSubmit, onEditClick }) => {
  const [formError, setFormError] = useState(null);
  const { formData } = useContext(FormDataContext);
  const { logData } = useContext(ApiContext);
  const {
    awsCloudWatchName,
    awsCloudWatchAwsKey,
    awsCloudWatchAwsRegion,
    awsCloudWatchKinesisStream,
    awsCloudWatchGlobalInput,
    awsCloudWatchAssumeARN,
    awsCloudWatchBatchSize,
    awsCloudWatchThrottleEnabled,
    awsCloudWatchAddFlowLogPrefix,
    awsCloudWatchKinesisInputType,
  } = formData;

  const globalInputEnabled = awsCloudWatchGlobalInput && awsCloudWatchGlobalInput.value;
  const throttleEnabled = awsCloudWatchThrottleEnabled && awsCloudWatchThrottleEnabled.value;
  const addPrefix = awsCloudWatchAddFlowLogPrefix && awsCloudWatchAddFlowLogPrefix.value;

  const [fetchSubmitStatus, setSubmitFetch] = useFetch(
    null,
    () => {
      onSubmit();
    },
    'POST',
    {
      name: awsCloudWatchName.value,
      region: awsCloudWatchAwsRegion.value,
      aws_input_type: awsCloudWatchKinesisInputType ? awsCloudWatchKinesisInputType.value : DEFAULT_KINESIS_LOG_TYPE,
      stream_name: awsCloudWatchKinesisStream.value,
      batch_size: Number(awsCloudWatchBatchSize.value || awsCloudWatchBatchSize.defaultValue),
      assume_role_arn: awsCloudWatchAssumeARN ? awsCloudWatchAssumeARN.value : '',
      global: globalInputEnabled,
      enable_throttling: throttleEnabled,
      add_flow_log_prefix: addPrefix,
    },
  );

  useEffect(() => {
    if (fetchSubmitStatus.error) {
      setFormError({
        full_message: fetchSubmitStatus.error,
        nice_message: <span>We were unable to save your Input, please try again in a few moments.</span>,
      });
    }
  }, [fetchSubmitStatus.error]);

  const handleSubmit = () => {
    setSubmitFetch(ApiRoutes.INTEGRATIONS.AWS.KINESIS.SAVE);
  };

  return (
    <FormWrap onSubmit={handleSubmit}
              buttonContent="Complete CloudWatch Setup"
              loading={fetchSubmitStatus.loading}
              error={formError}
              title="Final Review"
              description="Check out everything below to make sure it&apos;s correct, then click the button below to complete your CloudWatch setup!">

      <Container>
        <Subheader>Setting up CloudWatch <small><EditAnchor onClick={onEditClick('authorize')}>Edit</EditAnchor></small></Subheader>
        <ReviewItems>
          <li>
            <strong>Name</strong>
            <span>{awsCloudWatchName.value}</span>
          </li>
          <li>
            <strong>AWS Key</strong>
            <span>{awsCloudWatchAwsKey.value}</span>
          </li>
          <li>
            <strong>AWS Region</strong>
            <span>{awsCloudWatchAwsRegion.value}</span>
          </li>
        </ReviewItems>

        <Subheader>Setting up Kinesis <small><EditAnchor onClick={onEditClick('kinesis-setup')}>Edit</EditAnchor></small></Subheader>
        <ReviewItems>
          <li>
            <strong>Stream</strong>
            <span>{awsCloudWatchKinesisStream.value}</span>
          </li>
          <li>
            <strong>Global Input</strong>
            <span>{<i className={`fa fa-${globalInputEnabled ? 'check' : 'times'}`} />}</span>
          </li>
          <li>
            <strong>AWS Assumed ARN Role</strong>
            <span>{awsCloudWatchAssumeARN ? awsCloudWatchAssumeARN.value : 'None'}</span>
          </li>
          <li>
            <strong>Record Batch Size</strong>
            <span>
              {
                    awsCloudWatchBatchSize.value
                      ? awsCloudWatchBatchSize.value
                      : <Default value={awsCloudWatchBatchSize.defaultValue} />
                  }
            </span>
          </li>
          <li>
            <strong>Enable Throttling</strong>
            <span>{<i className={`fa fa-${throttleEnabled ? 'check' : 'times'}`} />}</span>
          </li>
          <li>
            <strong>Add Flow Log prefix to field names</strong>
            <span>{<i className={`fa fa-${addPrefix ? 'check' : 'times'}`} />}</span>
          </li>
        </ReviewItems>

        <Subheader>Formatting</Subheader>
        <ReviewItems>
          <li>
            <strong>Log Type</strong>
            <span>{KINESIS_LOG_TYPES.find(type => type.value === awsCloudWatchKinesisInputType.value).label}</span>
          </li>
        </ReviewItems>
        <p>If you need your logs parsed differently check out our <Link to={Routes.SYSTEM.PIPELINES.RULES}>Pipeline Rule</Link> for additional details and instructions.</p>

        <Input id="awsCloudWatchLog"
               type="textarea"
               label=""
               value={(logData && logData.message) || "We haven't received a response back from Amazon yet."}
               rows={10}
               disabled />
      </Container>
    </FormWrap>
  );
};

StepReview.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onEditClick: PropTypes.func.isRequired,
};

const Container = styled.div`
  border: 1px solid #A6AFBD;
  margin: 25px 0;
  padding: 15px;
  border-radius: 4px;
`;

const Subheader = styled.h3`
  margin: 0 0 10px;
`;

const ReviewItems = styled.ul`
  list-style: none;
  margin: 0 0 25px 10px;
  padding: 0;

  li {
    line-height: 2;
    padding: 0 5px;

    :nth-of-type(odd) {
      background-color: rgba(220, 225, 229, 0.4);
    }
  }

  strong {
    ::after {
      content: ':';
      margin-right: 5px;
    }
  }
`;

const EditAnchor = styled.a`
  font-size: 12px;
  margin-left: 5px;
  font-style: italic;
  cursor: pointer;

  ::before {
    content: "(";
  }

  ::after {
    content: ")";
  }
`;

export default StepReview;