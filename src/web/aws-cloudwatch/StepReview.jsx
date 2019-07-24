import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';
import { Col, Row } from 'react-bootstrap';
import { Link } from 'react-router';

import Routes from 'routing/Routes';
import { Input } from 'components/bootstrap';

import { FormDataContext } from './context/FormData';
import { ApiContext } from './context/Api';
import useFetch from './hooks/useFetch';

import FormWrap from '../common/FormWrap';
import { ApiRoutes } from '../common/Routes';

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
  const { formData } = useContext(FormDataContext);
  const { logSample } = useContext(ApiContext);
  const {
    awsCloudWatchName,
    awsCloudWatchDescription,
    awsCloudWatchAwsKey,
    awsCloudWatchAwsRegion,
    awsCloudWatchKinesisStream,
    awsCloudWatchGlobalInput,
    awsCloudWatchAssumeARN,
    awsCloudWatchBatchSize,
    awsCloudWatchThrottleEnabled,
    awsCloudWatchThrottleWait,
  } = formData;

  const throttleEnabled = awsCloudWatchThrottleEnabled && awsCloudWatchThrottleEnabled.value && awsCloudWatchThrottleWait;
  const throttleValue = throttleEnabled ? awsCloudWatchThrottleWait.value : awsCloudWatchThrottleWait.defaultValue;

  const [fetchSubmitStatus, setSubmitFetch] = useFetch(
    null,
    () => {
      onSubmit();
    },
    'POST',
    {
      name: awsCloudWatchName.value,
      description: awsCloudWatchDescription ? awsCloudWatchDescription.value : '',
      region: awsCloudWatchAwsRegion.value,
      aws_input_type: 'KINESIS_FLOW_LOGS',
      stream_name: awsCloudWatchKinesisStream.value,
      batch_size: Number(awsCloudWatchBatchSize.value || awsCloudWatchBatchSize.defaultValue),
      assume_role_arn: awsCloudWatchAssumeARN ? awsCloudWatchAssumeARN.value : '',
      global: (awsCloudWatchGlobalInput && awsCloudWatchGlobalInput.value),
      enable_throttling: throttleEnabled,
      kinesis_max_throttled_wait_ms: Number(throttleValue),
    },
  );

  const handleSubmit = () => {
    setSubmitFetch(ApiRoutes.INTEGRATIONS.AWS.KINESIS.SAVE);
  };

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={handleSubmit} buttonContent="Complete CloudWatch Setup" loading={fetchSubmitStatus.loading}>
          <h2>Final Review</h2>

          <p>Check out everything below to make sure it&apos;s correct, then click the button below to complete your CloudWatch setup!</p>

          <Container>
            <Subheader>Setting up CloudWatch <small><EditAnchor onClick={onEditClick('authorize')}>Edit</EditAnchor></small></Subheader>
            <ReviewItems>
              <li>
                <strong>Name</strong>
                <span>{awsCloudWatchName.value}</span>
              </li>
              {
                awsCloudWatchDescription
                && (
                  <li>
                    <strong>Description</strong>
                    <span>{awsCloudWatchDescription.value || ''}</span>
                  </li>
                )
              }
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
                <span>{(awsCloudWatchGlobalInput && awsCloudWatchGlobalInput.value) ? <i className="fa fa-check" /> : <i className="fa fa-times" />}</span>
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
                <strong>Throttled Wait (ms)</strong>
                <span>
                  {
                    throttleEnabled ? awsCloudWatchThrottleWait.value : <Default value={awsCloudWatchThrottleWait.defaultValue} />
                  }
                </span>
              </li>
            </ReviewItems>

            <Subheader>Formatting <FormatIcon success><i className="fa fa-smile-o" /></FormatIcon></Subheader>
            <p>Parsed as LogFlow, if you need a different type you&apos;ll need to setup a <Link to={Routes.SYSTEM.PIPELINES.RULES}>Pipeline Rule</Link>.</p>

            <Input id="awsCloudWatchLog"
                   type="textarea"
                   label=""
                   value={logSample}
                   rows={10}
                   disabled />
          </Container>
        </FormWrap>
      </Col>
    </Row>
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

const FormatIcon = styled.span`
  color: ${props => (props.success ? '#00AE42' : '#AD0707')};
  margin-left: 10px;
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
