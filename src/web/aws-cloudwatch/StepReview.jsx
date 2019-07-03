import React from 'react';
import PropTypes from 'prop-types';
import styled from '@emotion/styled';
import { Col, Row } from 'react-bootstrap';
import { Link } from 'react-router';

import Routes from 'routing/Routes';
import { Input } from 'components/bootstrap';

import formDataHook from './hooks/formData';

import FormWrap from '../common/FormWrap';

const StepReview = ({ onSubmit, onEditClick, logOutput }) => {
  const { getFieldData, getFieldValue } = formDataHook();

  const defaultOutput = (key, enabled = true) => {
    const fieldData = getFieldData(key);
    const outputValue = getFieldValue(key);
    const Default = <small>(default)</small>;
    const isDefault = (fieldData.defaultValue === fieldData.value) || !enabled;

    return (
      <React.Fragment>
        {enabled ? outputValue : fieldData.defaultValue} {isDefault && Default}
      </React.Fragment>
    );
  };

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={onSubmit} buttonContent="Complete CloudWatch Setup">
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
            <p>Parsed as LogFlow, if you need a different type you&apos;ll need to setup a <Link to={Routes.SYSTEM.PIPELINES.RULES}>Pipeline Rule</Link>.</p>

            <Input id="awsCloudWatchLog"
                   type="textarea"
                   label=""
                   value={logOutput}
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
  logOutput: PropTypes.string.isRequired,
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
