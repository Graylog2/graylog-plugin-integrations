import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import FormAdvancedOptions from './FormAdvancedOptions';
import { FormDataContext } from './context/FormData';
import { ApiContext } from './context/Api';
import useFetch from './hooks/useFetch';

import FormWrap from '../common/FormWrap';
import ValidatedSelect from '../common/ValidatedSelect';
import Routes, { ApiRoutes } from '../common/Routes';
import { renderOptions } from '../common/Options';

const KinesisStreams = ({ onChange, onSubmit }) => {
  const { formData } = useContext(FormDataContext);
  const { availableStreams, setLogSample } = useContext(ApiContext);
  const [logSampleStatus, setLogSampleUrl] = useFetch(
    null,
    (response) => {
      setLogSample(response);
      onSubmit();
    },
    'POST',
    {
      region: formData.awsCloudWatchAwsRegion.value,
      stream_name: formData.awsCloudWatchKinesisStream ? formData.awsCloudWatchKinesisStream.value : '',
    },
  );

  const handleSubmit = () => {
    setLogSampleUrl(ApiRoutes.INTEGRATIONS.AWS.KINESIS.HEALTH_CHECK);
  };

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={handleSubmit}
                  buttonContent="Verify Stream &amp; Format"
                  loading={logSampleStatus.loading}
                  required={['awsCloudWatchKinesisStream']}
                  context={formData}>
          <h2>Choose Kinesis Stream</h2>
          <p>Below is a list of all Kinesis Streams found within the specified AWS account. Please choose the Stream you would like us to read messages from, or follow the directions to begin <a href={Routes.INTEGRATIONS.AWS.CLOUDWATCH.step('kinesis-setup')}>setting up your CloudWatch Log Group</a> to feed messages into a new Kinesis Stream.</p>


          <ValidatedSelect id="awsCloudWatchKinesisStream"
                           label="Choose Stream"
                           placeHolder="Choose Kinesis Stream"
                           options={availableStreams}
                           onChange={onChange}
                           value={formData.awsCloudWatchKinesisStream && formData.awsCloudWatchKinesisStream.value} />

          <FormAdvancedOptions onChange={onChange} />
        </FormWrap>
      </Col>
    </Row>
  );
};

KinesisStreams.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
};

export default KinesisStreams;
