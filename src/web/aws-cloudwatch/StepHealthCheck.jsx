import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';
import styled from '@emotion/styled';

import { Input } from 'components/bootstrap';

import { ApiContext } from './context/Api';

import FormWrap from '../common/FormWrap';
import LogSampleIcon from '../common/LogSampleIcon';

const StepHealthCheck = ({ onSubmit }) => {
  const { logSample } = useContext(ApiContext);

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={onSubmit} buttonContent="Review &amp; Finalize">
          <h2>Create Kinesis Stream</h2>
          <p>We&apos;re going to attempt to parse a single log to help you out! If we&apos;re unable to, or you would like it parsed differently, head on over to <a href="/system/pipelines">Pipeline Rules</a> to set up your own parser!</p>

          <Feedback><LogIcon success /> Great! Looks like a well formatted Flow Log.</Feedback>

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

const Feedback = styled.p`
  display: flex;
  align-items: center;
  font-weight: bold;
`;

const LogIcon = styled(LogSampleIcon)`
  margin-right: 5px;
`;

export default StepHealthCheck;
