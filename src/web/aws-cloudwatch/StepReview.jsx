import React from 'react';
import PropTypes from 'prop-types';
import { Button, Col, Row } from 'react-bootstrap';
import styled from '@emotion/styled';

import { Input } from 'components/bootstrap';

import { DEFAULT_SETTINGS } from './util';

const StepReview = ({ values, onSubmit, logOutput }) => {
  const defaultOutput = (key, enabled = true) => {
    if (!enabled) {
      return (
        <React.Fragment>
          {DEFAULT_SETTINGS[key]} <small>(default)</small>
        </React.Fragment>
      );
    }

    return (
      <React.Fragment>
        {values[key]} {DEFAULT_SETTINGS[key] === values[key] && <small>(default)</small>}
      </React.Fragment>
    );
  };

  return (
    <Row>
      <Col md={8}>
        <form onSubmit={onSubmit}>
          <h2>Final Review</h2>

          <p>Check out everything below to make sure it&apos;s correct, then click the button below to complete your CloudWatch setup!</p>

          <Container>
            <Subheader>Setting up CloudWatch <small><button type="button">Edit</button></small></Subheader>
            <ReviewItems>
              <li><strong>Name</strong><span>{values.awsCloudWatchName}</span></li>
              { values.awsCloudWatchDescription
                && <li><strong>Description</strong><span>{values.awsCloudWatchDescription}</span></li>
              }
              <li><strong>AWS Key</strong><span>{values.awsCloudWatchAwsKey}</span></li>
              <li><strong>AWS Secret</strong><span>{values.awsCloudWatchAwsSecret}</span></li>
              <li><strong>AWS Region</strong><span>{values.awsCloudWatchAwsRegion}</span></li>
            </ReviewItems>

            <Subheader>Setting up Kinesis <small><button type="button">Edit</button></small></Subheader>
            <ReviewItems>
              <li><strong>Stream</strong><span>{values.awsCloudWatchKinesisStream}</span></li>
              <li><strong>Global Input</strong><span>{values.awsCloudWatchGlobalInput ? 'true' : 'false'}</span></li>
              <li><strong>AWS Assumed ARN Role</strong><span>{values.awsCloudWatchAssumeARN || 'None'}</span></li>
              <li>
                <strong>Record Batch Size</strong>
                <span>{defaultOutput('awsCloudWatchBatchSize')}</span>
              </li>
              <li>
                <strong>Throttled Wait (ms)</strong>
                <span>{defaultOutput('awsCloudWatchThrottleWait', values.awsCloudWatchThrottleEnabled)}</span>
              </li>
            </ReviewItems>

            <Subheader>Formatting <FormatIcon success><i className="fa fa-smile-o" /></FormatIcon></Subheader>
            <p>Parsed as LogFlow, if you need a different type you&apos;ll need to setup a <a href="/pipeline">Pipeline Rule</a>.</p>

            <Input id="awsCloudWatchLog"
                   type="textarea"
                   label=""
                   value={logOutput}
                   rows={10}
                   disabled />
          </Container>

          <Button type="submit" bsStyle="primary">Complete CloudWatch Setup</Button>
        </form>
      </Col>
    </Row>
  );
};

StepReview.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  values: PropTypes.object.isRequired,
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

    :nth-child(odd) {
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

export default StepReview;
