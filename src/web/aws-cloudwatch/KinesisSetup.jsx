import React, { useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import ValidatedInput from '../common/ValidatedInput';
import FormWrap from '../common/FormWrap';
import useFetch from '../common/hooks/useFetch';
import { ApiRoutes } from '../common/Routes';
import { renderOptions } from '../common/Options';
import formValidation from '../utils/formValidation';

import FormAdvancedOptions from './FormAdvancedOptions';
import { FormDataContext } from './context/FormData';
import { ApiContext } from './context/Api';

const KinesisSetup = ({ onChange, onSubmit }) => {
  const { availableGroups, setGroups } = useContext(ApiContext);
  const { formData } = useContext(FormDataContext);
  const [formError, setFormError] = useState(null);
  const [groupNamesStatus, setGroupNamesUrl] = useFetch(
    ApiRoutes.INTEGRATIONS.AWS.CLOUDWATCH.GROUPS,
    (response) => {
      setGroups(response);
    },
    'POST',
    { region: formData.awsCloudWatchAwsRegion.value },
  );

  useEffect(() => {
    if (groupNamesStatus.error) {
      setGroupNamesUrl(null);

      const noGroups = /No CloudWatch log groups/g;
      if (groupNamesStatus.error.match(noGroups)) {
        setFormError({
          full_message: groupNamesStatus.error,
          nice_message: <span>We&apos;re unable to find any groups in your chosen region. Please try choosing a different region, or follow this <a href="/">CloudWatch documentation</a> to begin setting up your AWS CloudWatch account.</span>,
        });
      } else {
        setFormError({
          full_message: groupNamesStatus.error,
        });
      }
    }
  }, [groupNamesStatus.error]);

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={onSubmit}
                  buttonContent="Verify &amp; Format"
                  disabled={formValidation.isFormValid([
                    'awsCloudWatchKinesisStream',
                    'awsCloudWatchAwsGroupName',
                  ], formData)}
                  loading={groupNamesStatus.loading}
                  error={formError}
                  title="Create Kinesis Stream"
                  description="We were unable to find any Kinesis Streams, but we could find some Groups. Complete the fields below and we will setup your Stream for you!">

          <ValidatedInput id="awsCloudWatchKinesisStream"
                          type="text"
                          label="Kinesis Stream Name"
                          placeholder="Create Stream Name"
                          onChange={onChange}
                          fieldData={formData.awsCloudWatchKinesisStream}
                          required />

          <ValidatedInput id="awsCloudWatchAwsGroupName"
                          type="select"
                          fieldData={formData.awsCloudWatchAwsGroupName}
                          onChange={onChange}
                          label="CloudWatch Group Name"
                          required
                          disabled={groupNamesStatus.loading}>

            {renderOptions(availableGroups, 'Choose CloudWatch Group', groupNamesStatus.loading)}
          </ValidatedInput>

          <FormAdvancedOptions onChange={onChange} />
        </FormWrap>
      </Col>
    </Row>
  );
};

KinesisSetup.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
};

export default KinesisSetup;
