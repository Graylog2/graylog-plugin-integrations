import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Button, Col, Row } from 'react-bootstrap';

import { Input } from 'components/bootstrap';

const StepHealthCheck = ({ onSubmit, logOutput }) => {
  return (
    <Row>
      <Col md={8}>
        <form onSubmit={onSubmit}>
          <h2>Create Kinesis Stream</h2>
          <p>We&apos;re going to attempt to parse a single log to help you out! If we&apos;re unable to, or you would like it parsed differently, head on over to <a href="/system/pipelines">Pipeline Rules</a> to set up your own parser!</p>

          <span><i className="fa fa-smile-o fa-2x" /> Great! Looks like a well formatted Flow Log.</span>

          <Input id="awsCloudWatchLog"
                 type="textarea"
                 label="Formatted CloudWatch Log"
                 value={logOutput}
                 disabled />

          <Button type="submit">Review &amp; Finalize</Button>
        </form>
      </Col>
    </Row>
  );
};

StepHealthCheck.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  logOutput: PropTypes.string.isRequired,
};

export default StepHealthCheck;
