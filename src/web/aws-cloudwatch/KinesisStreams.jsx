import React, { useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import FormAdvancedOptions from './FormAdvancedOptions';
import { FormDataContext } from './context/FormData';
import { ApiContext } from './context/Api';
import useFetch from '../common/hooks/useFetch';

import FormWrap from '../common/FormWrap';
import ValidatedInput from '../common/ValidatedInput';
import Routes, { ApiRoutes } from '../common/Routes';
import { renderOptions } from '../common/Options';
import formValidation from '../utils/formValidation';

const KinesisStreams = ({ onChange, onSubmit }) => {
  const { formData } = useContext(FormDataContext);
  const [formError, setFormError] = useState(null);
  const { availableStreams, setLogData } = useContext(ApiContext);
  const [logDataStatus, setLogDataUrl] = useFetch(
    null,
    (response) => {
      setLogData(response);
      onSubmit();
    },
    'POST',
    {
      region: formData.awsCloudWatchAwsRegion.value,
      stream_name: formData.awsCloudWatchKinesisStream ? formData.awsCloudWatchKinesisStream.value : '',
    },
  );

  useEffect(() => {
    if (logDataStatus.error) {
      setLogDataUrl(null);
      setFormError({
        full_message: logDataStatus.error,
        nice_message: <span>We were unable to find any logs in this Kinesis Stream. Please choose a different stream.</span>,
      });
    }
  }, [logDataStatus.error]);

  const handleSubmit = () => {
    setLogDataUrl(ApiRoutes.INTEGRATIONS.AWS.KINESIS.HEALTH_CHECK);
  };

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={handleSubmit}
                  buttonContent="Verify Stream &amp; Format"
                  loading={logDataStatus.loading}
                  error={formError}
                  disabled={formValidation.isFormValid(['awsCloudWatchKinesisStream'], formData)}
                  title="Choose Kinesis Stream"
                  description={<p>Below is a list of all Kinesis Streams found within the specified AWS account. Please choose the Stream you would like us to read messages from, or follow the directions to begin <a href={Routes.INTEGRATIONS.AWS.CLOUDWATCH.step('kinesis-setup')}>setting up your CloudWatch Log Group</a> to feed messages into a new Kinesis Stream.</p>}>

          <ValidatedInput id="awsCloudWatchKinesisStream"
                          type="select"
                          fieldData={formData.awsCloudWatchKinesisStream}
                          onChange={onChange}
                          label="Choose Stream"
                          required>
            {renderOptions(availableStreams, 'Choose Kinesis Stream')}
          </ValidatedInput>

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
