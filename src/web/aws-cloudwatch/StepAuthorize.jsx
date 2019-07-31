import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';
import styled from 'styled-components';

import { FormDataContext } from './context/FormData';
import { ApiContext } from './context/Api';
import useFetch from './hooks/useFetch';

import ValidatedInput from '../common/ValidatedInput';
import FormWrap from '../common/FormWrap';
import { renderOptions } from '../common/Options';
import { ApiRoutes } from '../common/Routes';
import formValidation from '../utils/formValidation';

const StepAuthorize = ({ onChange, onSubmit }) => {
  const { formData } = useContext(FormDataContext);
  const { availableRegions, setRegions, setStreams } = useContext(ApiContext);
  const [fetchRegionsStatus] = useFetch(ApiRoutes.INTEGRATIONS.AWS.REGIONS, setRegions, 'GET');
  const [fetchStreamsStatus, setStreamsFetch] = useFetch(
    null,
    (response) => {
      setStreams(response);
      onSubmit();
    },
    'POST',
    { region: formData.awsCloudWatchAwsRegion ? formData.awsCloudWatchAwsRegion.value : '' },
  );

  const handleSubmit = () => {
    setStreamsFetch(ApiRoutes.INTEGRATIONS.AWS.KINESIS.STREAMS);
  };

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={handleSubmit}
                  buttonContent="Authorize &amp; Choose Stream"
                  loading={fetchRegionsStatus.loading || fetchStreamsStatus.loading}
                  disabled={formValidation.isFormValid([
                    'awsCloudWatchName',
                    'awsCloudWatchAwsKey',
                    'awsCloudWatchAwsSecret',
                    'awsCloudWatchAwsRegion',
                  ], formData)}
                  title="Create Integration &amp; Authorize AWS"
                  description="Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ipsum facere quis maiores doloribus asperiores modi dignissimos enim accusamus sunt aliquid, pariatur eligendi esse dolore temporibus corporis corrupti dolorum, soluta consectetur?">

          {/* Fighting AutoComplete Forms */}
          <DisappearingInput id="name" type="text" />
          <DisappearingInput id="password" type="password" />
          {/* Continue on, Nothing to See Here */}

          <ValidatedInput id="awsCloudWatchName"
                          type="text"
                          fieldData={formData.awsCloudWatchName}
                          onChange={onChange}
                          placeholder="CloudWatch Integration Name"
                          label="Name of integration"
                          autoComplete="off"
                          required />

          <ValidatedInput id="awsCloudWatchDescription"
                          type="textarea"
                          label="Integration description"
                          placeholder="CloudWatch Integration Description"
                          onChange={onChange}
                          fieldData={formData.awsCloudWatchDescription}
                          rows={4} />

          <ValidatedInput id="awsCloudWatchAwsKey"
                          type="text"
                          label="AWS Key"
                          placeholder="CloudWatch Integration AWS Key"
                          onChange={onChange}
                          fieldData={formData.awsCloudWatchAwsKey}
                          autoComplete="off"
                          maxLength="512"
                          help='Your AWS Key should be a 20-character long, alphanumeric string that starts with the letters "AK".'
                          required />

          <ValidatedInput id="awsCloudWatchAwsSecret"
                          type="password"
                          label="AWS Secret"
                          placeholder="CloudWatch Integration AWS Secret"
                          onChange={onChange}
                          fieldData={formData.awsCloudWatchAwsSecret}
                          autoComplete="off"
                          maxLength="512"
                          help="Your AWS Secret is usually a 40-character long, base-64 encoded string."
                          required />

          <ValidatedInput id="awsCloudWatchAwsRegion"
                          type="select"
                          fieldData={formData.awsCloudWatchAwsRegion}
                          onChange={onChange}
                          label="Region"
                          help="Provide the region your CloudWatch instance is deployed."
                          disabled={fetchRegionsStatus.loading}
                          required>
            {renderOptions(availableRegions, 'Choose AWS Region', fetchRegionsStatus.loading)}
          </ValidatedInput>
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
