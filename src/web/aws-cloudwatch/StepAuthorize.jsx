import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';
import styled from 'styled-components';

import { FormDataContext } from './context/FormData';
import { ApiContext } from './context/Api';
import useFetch from './hooks/useFetch';

import ValidatedInput from '../common/ValidatedInput';
import ValidatedSelect from '../common/ValidatedSelect';
import FormWrap from '../common/FormWrap';
import { ApiRoutes } from '../common/Routes';

const StepAuthorize = ({ onChange, onSubmit }) => {
  const { formData } = useContext(FormDataContext);
  const {
    awsCloudWatchName,
    awsCloudWatchDescription,
    awsCloudWatchAwsKey,
    awsCloudWatchAwsSecret,
    awsCloudWatchAwsRegion,
  } = formData;
  const { availableRegions, setRegions, setStreams } = useContext(ApiContext);
  const [fetchRegionsStatus] = useFetch(ApiRoutes.INTEGRATIONS.AWS.REGIONS, setRegions, 'GET');
  const [fetchStreamsStatus, setStreamsFetch] = useFetch(
    null,
    (response) => {
      setStreams(response);
      onSubmit();
    },
    'POST',
    { region: awsCloudWatchAwsRegion ? awsCloudWatchAwsRegion.value : '' },
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
                  required={[
                    'awsCloudWatchName',
                    'awsCloudWatchAwsKey',
                    'awsCloudWatchAwsSecret',
                    'awsCloudWatchAwsRegion',
                  ]}
                  context={formData}>
          <h2>Create Integration &amp; Authorize AWS</h2>
          <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ipsum facere quis maiores doloribus asperiores modi dignissimos enim accusamus sunt aliquid, pariatur eligendi esse dolore temporibus corporis corrupti dolorum, soluta consectetur?</p>

          {/* Fighting AutoComplete Forms */}
          <DisappearingInput id="name" type="text" />
          <DisappearingInput id="password" type="password" />
          {/* Continue on, Nothing to See Here */}

          <ValidatedInput id="awsCloudWatchName"
                          type="text"
                          fieldData={awsCloudWatchName}
                          onChange={onChange}
                          placeholder="CloudWatch Integration Name"
                          label="Name of integration"
                          autoComplete="off"
                          required />

          <ValidatedInput id="awsCloudWatchDescription"
                          type="textarea"
                          label={<>Integration description <small>(optional)</small></>}
                          placeholder="CloudWatch Integration Description"
                          onChange={onChange}
                          fieldData={awsCloudWatchDescription}
                          rows={4} />

          <ValidatedInput id="awsCloudWatchAwsKey"
                          type="text"
                          label="AWS Key"
                          placeholder="CloudWatch Integration AWS Key"
                          onChange={onChange}
                          fieldData={awsCloudWatchAwsKey}
                          autoComplete="off"
                          maxLength="512"
                          help='Your AWS Key should be a 20-character long, alphanumeric string that starts with the letters "AK".'
                          required />

          <ValidatedInput id="awsCloudWatchAwsSecret"
                          type="password"
                          label="AWS Secret"
                          placeholder="CloudWatch Integration AWS Secret"
                          onChange={onChange}
                          fieldData={awsCloudWatchAwsSecret}
                          autoComplete="off"
                          maxLength="512"
                          help="Your AWS Secret is usually a 40-character long, base-64 encoded string."
                          required />

          <ValidatedSelect id="awsCloudWatchAwsRegion"
                           label="AWS Region"
                           placeHolder="Choose a Region..."
                           options={availableRegions}
                           onChange={onChange}
                           value={awsCloudWatchAwsRegion && awsCloudWatchAwsRegion.value}
                           menuPlacement="top" />
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
