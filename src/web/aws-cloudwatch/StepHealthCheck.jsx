import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import { Input } from 'components/bootstrap';

import { ApiContext } from './context/Api';

import FormWrap from '../common/FormWrap';

const StepHealthCheck = ({ onSubmit }) => {
  const { logSample } = useContext(ApiContext);

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={onSubmit}
                  buttonContent="Review &amp; Finalize"
                  disabled={false}
                  title="Create Kinesis Stream"
                  description={<p>We&apos;re going to attempt to parse a single log to help you out! If we&apos;re unable to, or you would like it parsed differently, head on over to <a href="/system/pipelines">Pipeline Rules</a> to set up your own parser!</p>}>

          <span><i className="fa fa-smile-o fa-2x" /> Great! Looks like a well formatted Flow Log.</span>

          <Input id="awsCloudWatchLog"
                 type="textarea"
                 label="Formatted CloudWatch Log"
                 value={logSample}
                 rows={10}
                 disabled />
        </FormWrap>
      </Col>
    </Row>
  );
};

StepHealthCheck.propTypes = {
  onSubmit: PropTypes.func.isRequired,
};

export default StepHealthCheck;
