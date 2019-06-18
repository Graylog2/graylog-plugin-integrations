import React from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';
import styled from '@emotion/styled';

import formDataHook from './hooks/formData';
import StyledForm from '../common/StyledForm';
import StyledInput from '../common/StyledInput';

const StepAuthorize = ({ onChange, onSubmit }) => {
  const { getFieldValue } = formDataHook();

  return (
    <Row>
      <Col>
        <StyledForm onSubmit={onSubmit} buttonContent="Authorize &amp; Choose Stream">
          <h2>Create Integration &amp; Authorize AWS</h2>
          <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ipsum facere quis maiores doloribus asperiores modi dignissimos enim accusamus sunt aliquid, pariatur eligendi esse dolore temporibus corporis corrupti dolorum, soluta consectetur?</p>

          {/* Fighting AutoComplete Forms */}
          <DisappearingInput id="name" type="text" />
          <DisappearingInput id="password" type="password" />
          {/* Continue on, Nothing to See Here */}

          <StyledInput id="awsCloudWatchName"
                       type="text"
                       value={getFieldValue('awsCloudWatchName')}
                       onChange={onChange}
                       placeholder="CloudWatch Integration Name"
                       label="Name"
                       autoComplete="off"
                       required />

          <StyledInput id="awsCloudWatchDescription"
                       type="textarea"
                       label="Description"
                       placeholder="CloudWatch Integration Description"
                       onChange={onChange}
                       value={getFieldValue('awsCloudWatchDescription')}
                       rows={4} />

          <StyledInput id="awsCloudWatchAwsKey"
                       type="password"
                       label="AWS Key"
                       placeholder="CloudWatch Integration AWS Key"
                       onChange={onChange}
                       value={getFieldValue('awsCloudWatchAwsKey')}
                       autoComplete="off"
                       pattern="AK[A-Z0-9]{18}"
                       minLength="20"
                       required />

          <StyledInput id="awsCloudWatchAwsSecret"
                       type="password"
                       label="AWS Secret"
                       placeholder="CloudWatch Integration AWS Secret"
                       onChange={onChange}
                       value={getFieldValue('awsCloudWatchAwsSecret')}
                       autoComplete="off"
                       pattern="[A-Za-z0-9/+=]{40}"
                       minLength="40"
                       required />

          <StyledInput id="awsCloudWatchAwsRegion"
                       type="select"
                       value={getFieldValue('awsCloudWatchAwsRegion')}
                       onChange={onChange}
                       label="Region"
                       required>
            <option value="">Choose Region</option>
            <option value="us-east-2">US East (Ohio)</option>
            <option value="us-east-1">US East (N. Virginia)</option>
            <option value="us-west-1">US West (N. California)</option>
            <option value="us-west-2">US West (Oregon)</option>
          </StyledInput>
        </StyledForm>
      </Col>
    </Row>
  );
};

StepAuthorize.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
};

const DisappearingInput = styled.input`
  position: fixed;
  top: -500vh;
  left: -500vw;
`;

export default StepAuthorize;
