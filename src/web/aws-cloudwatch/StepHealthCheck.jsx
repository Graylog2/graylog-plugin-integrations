import React from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import { Input } from 'components/bootstrap';

import StyledForm from '../common/StyledForm';
import logHook from './hooks/log';

const StepHealthCheck = ({ onSubmit }) => {
  const { getLog } = logHook();

  return (
    <Row>
      <Col>
        <StyledForm onSubmit={onSubmit} buttonContent="Review &amp; Finalize">
          <h2>Create Kinesis Stream</h2>
          <p>We&apos;re going to attempt to parse a single log to help you out! If we&apos;re unable to, or you would like it parsed differently, head on over to <a href="/system/pipelines">Pipeline Rules</a> to set up your own parser!</p>

          <span><i className="fa fa-smile-o fa-2x" /> Great! Looks like a well formatted Flow Log.</span>

          <Input id="awsCloudWatchLog"
                 type="textarea"
                 label="Formatted CloudWatch Log"
                 value={getLog()}
                 rows={10}
                 disabled />
        </StyledForm>
      </Col>
    </Row>
  );
};

StepHealthCheck.propTypes = {
  onSubmit: PropTypes.func.isRequired,
};

export default StepHealthCheck;
