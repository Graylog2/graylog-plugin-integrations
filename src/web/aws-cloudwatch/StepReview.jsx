import React from 'react';
import PropTypes from 'prop-types';
import { Button, Col, Row } from 'react-bootstrap';

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

          <p>Check out everything below to make sure it&apros;s correct, then click the button below to complete your CloudWatch setup!</p>

          <div>
            <h3>Setting up CloudWatch <small><button type="button">Edit</button></small></h3>
            <ul>
              <li><strong>Name</strong><span>{values.awsCloudWatchName}</span></li>
              { values.awsCloudWatchDescription
                && <li><strong>Description</strong><span>{values.awsCloudWatchDescription}</span></li>
              }
              <li><strong>AWS Key</strong><span>{values.awsCloudWatchAwsKey}</span></li>
              <li><strong>AWS Secret</strong><span>{values.awsCloudWatchAwsSecret}</span></li>
              <li><strong>AWS Region</strong><span>{values.awsCloudWatchAwsRegion}</span></li>
            </ul>

            <h3>Setting up Kinesis <small><button type="button">Edit</button></small></h3>
            <ul>
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
            </ul>

            <h3>Formatting <small><i className="fa fa-smile-o" /></small></h3>
            <p>Parsed as LogFlow, if you need a different type you&apros;ll need to setup a <a href="/pipeline">Pipeline Rule</a>.</p>

            <Input id="awsCloudWatchLog"
                   type="textarea"
                   label=""
                   value={logOutput}
                   disabled />
          </div>

          <Button type="submit">Complete CloudWatch Setup</Button>
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

export default StepReview;
