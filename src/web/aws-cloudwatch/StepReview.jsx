import React from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';
import styled from '@emotion/styled';
import { Link } from 'react-router';

import Routes from 'routing/Routes';

import DEFAULT_VALUES from './default_values';

import StyledForm from '../common/StyledForm';
import StyledInput from '../common/StyledInput';

import formDataHook from './hooks/formData';
import logHook from './hooks/log';

const StepReview = ({ onSubmit, onEditClick }) => {
  const { getFieldData, getFieldValue } = formDataHook();
  const { getLog } = logHook();

  const defaultOutput = (key, enabled = true) => {
    const fieldData = getFieldData(key);
    if (!enabled) {
      return (
        <React.Fragment>
          {fieldData.defaultValue} <small>(default)</small>
        </React.Fragment>
      );
    }

    return (
      <React.Fragment>
        {fieldData.value} {fieldData.defaultValue === fieldData.value && <small>(default)</small>}
      </React.Fragment>
    );
  };

  return (
    <Row>
      <Col>
        <StyledForm onSubmit={onSubmit} buttonContent="Complete CloudWatch Setup">
          <h2>Final Review</h2>

          <p>Check out everything below to make sure it&apos;s correct, then click the button below to complete your CloudWatch setup!</p>

          <Container>
            <Subheader>Setting up CloudWatch <small><EditAnchor onClick={onEditClick('authorize')}>Edit</EditAnchor></small></Subheader>
            <ReviewItems>
              <li><strong>Name</strong><span>{getFieldValue('awsCloudWatchName')}</span></li>
              { getFieldValue('awsCloudWatchDescription')
                && <li><strong>Description</strong><span>{getFieldValue('awsCloudWatchDescription')}</span></li>
              }
              <li><strong>AWS Key</strong><span>AK************{getFieldValue('awsCloudWatchAwsKey').slice(-6)}</span></li>
              {/* <li><strong>AWS Secret</strong><span>{getFieldValue('awsCloudWatchAwsSecret')}</span></li> */}
              <li><strong>AWS Region</strong><span>{getFieldValue('awsCloudWatchAwsRegion')}</span></li>
            </ReviewItems>

            <Subheader>Setting up Kinesis <small><EditAnchor onClick={onEditClick('kinesis-setup')}>Edit</EditAnchor></small></Subheader>
            <ReviewItems>
              <li><strong>Stream</strong><span>{getFieldValue('awsCloudWatchKinesisStream')}</span></li>
              <li><strong>Global Input</strong><span>{getFieldValue('awsCloudWatchGlobalInput') ? 'true' : 'false'}</span></li>
              <li><strong>AWS Assumed ARN Role</strong><span>{getFieldValue('awsCloudWatchAssumeARN') || 'None'}</span></li>
              <li>
                <strong>Record Batch Size</strong>
                <span>{defaultOutput('awsCloudWatchBatchSize')}</span>
              </li>
              <li>
                <strong>Throttled Wait (ms)</strong>
                <span>{defaultOutput('awsCloudWatchThrottleWait', getFieldValue('awsCloudWatchThrottleEnabled'))}</span>
              </li>
            </ReviewItems>

            <Subheader>Formatting <FormatIcon success><i className="fa fa-smile-o" /></FormatIcon></Subheader>
            <p>Parsed as FlowLog, if you need a different type you&apos;ll need to setup a <Link to={Routes.SYSTEM.PIPELINES.RULES}>Pipeline Rule</Link>.</p>

            <StyledInput id="awsCloudWatchLog"
                         type="textarea"
                         label=""
                         value={getLog()}
                         rows={10}
                         disabled />
          </Container>
        </StyledForm>
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
