import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';
import styled from '@emotion/styled';

import { FormDataContext } from './context/FormData';

import StyledInput from '../common/StyledInput';
import FormWrap from '../common/FormWrap';

const StepAuthorize = ({ onChange, onSubmit }) => {
  const { formData } = useContext(FormDataContext);

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={onSubmit} buttonContent="Authorize &amp; Choose Stream">
          <h2>Create Integration &amp; Authorize AWS</h2>
          <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ipsum facere quis maiores doloribus asperiores modi dignissimos enim accusamus sunt aliquid, pariatur eligendi esse dolore temporibus corporis corrupti dolorum, soluta consectetur?</p>

          {/* Fighting AutoComplete Forms */}
          <DisappearingInput id="name" type="text" />
          <DisappearingInput id="password" type="password" />
          {/* Continue on, Nothing to See Here */}

          <StyledInput id="awsCloudWatchName"
                       type="text"
                       value={formData.awsCloudWatchName ? formData.awsCloudWatchName.value : ''}
                       onChange={onChange}
                       placeholder="CloudWatch Integration Name"
                       label="Name of integration"
                       autoComplete="off"
                       required />

          <StyledInput id="awsCloudWatchDescription"
                       type="textarea"
                       label="Integration description"
                       placeholder="CloudWatch Integration Description"
                       onChange={onChange}
                       value={formData.awsCloudWatchDescription ? formData.awsCloudWatchDescription.value : ''}
                       rows={4} />

          <StyledInput id="awsCloudWatchAwsKey"
                       type="password"
                       label="AWS Key"
                       placeholder="CloudWatch Integration AWS Key"
                       onChange={onChange}
                       value={formData.awsCloudWatchAwsKey ? formData.awsCloudWatchAwsKey.value : ''}
                       autoComplete="off"
                       pattern="AK[A-Z0-9]{18}"
                       minLength="20"
                       help='Your AWS Key will be 20-character long, alphanumeric string that starts with the letters "AK".'
                       required />

          <StyledInput id="awsCloudWatchAwsSecret"
                       type="password"
                       label="AWS Secret"
                       placeholder="CloudWatch Integration AWS Secret"
                       onChange={onChange}
                       value={formData.awsCloudWatchAwsSecret ? formData.awsCloudWatchAwsSecret.value : ''}
                       autoComplete="off"
                       pattern="[A-Za-z0-9/+=]{40}"
                       minLength="40"
                       help="Your AWS Secret will be a 40-character long, base-64 encoded string."
                       required />

          <StyledInput id="awsCloudWatchAwsRegion"
                       type="select"
                       value={formData.awsCloudWatchAwsRegion ? formData.awsCloudWatchAwsRegion.value : ''}
                       onChange={onChange}
                       label="Region"
                       help="Provide the region your CloudWatch instance is deployed."
                       required>
            <option value="">Choose Region</option>
            <option value="us-east-2">US East (Ohio)</option>
            <option value="us-east-1">US East (N. Virginia)</option>
            <option value="us-west-1">US West (N. California)</option>
            <option value="us-west-2">US West (Oregon)</option>
          </StyledInput>
        </FormWrap>
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
