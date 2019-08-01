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

const KinesisSetup = ({ onChange, onSubmit, toggleSetup }) => {
  const { availableGroups, setGroups } = useContext(ApiContext);
  const { formData } = useContext(FormDataContext);
  const [ formError, setFormError ] = useState(null);
  const [ disabledGroups, setDisabledGroups ] = useState(false);
  const [ groupNamesStatus, setGroupNamesUrl ] = useFetch(
    ApiRoutes.INTEGRATIONS.AWS.CLOUDWATCH.GROUPS,
    (response) => {
      setGroups(response);
    },
    'POST',
    { region: formData.awsCloudWatchAwsRegion.value },
  );
  const [ createStreamStatus, createStreamFetch ] = useFetch(
    null,
    (response) => {
      console.log("Stream creation complete: []", response, formData);
      createPolicyFetch(ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_SUBSCRIPTION_POLICY);
    },
    'POST',
    {
      region: formData.awsCloudWatchAwsRegion.value,
      stream_name: formData.awsCloudWatchKinesisStream ? formData.awsCloudWatchKinesisStream.value : '',
    },
  );
  const [ createPolicyStatus, createPolicyFetch ] = useFetch(
    null,
    (response) => {
      console.log("Policy creation complete: []", response);
      createSubscriptionFetch(ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_SUBSCRIPTION);
    },
    'POST',
    {
      region: formData.awsCloudWatchAwsRegion.value,
      role_name: 'role-name',
      stream_name: 'test-stream',
      stream_arn: 'test-stream-arn',
    },
  );
  const [ createSubscriptionStatus, createSubscriptionFetch ] = useFetch(
    null,
    (response) => {
      console.log("Subscription creation complete: []", response);
    },
    'POST',
    {
      region: formData.awsCloudWatchAwsRegion.value,
      log_group_name: 'log-group',
      filter_name: 'filter-name',
      filter_pattern: 'pattern',
      destination_stream_arn: 'stream-arn',
      role_arn: 'role-arn',
    },
  );

  useEffect(() => {
    if (groupNamesStatus.error) {
      setGroupNamesUrl(null);

      const noGroups = /No CloudWatch log groups/g;
      if (groupNamesStatus.error.match(noGroups)) {
        setFormError({
          full_message: groupNamesStatus.error,
          nice_message: <span>We&apos;re unable to find any groups in your chosen region. Please try choosing a different region, or follow this <a
            href="/">CloudWatch documentation</a> to begin setting up your AWS CloudWatch account.</span>,
        });
        setDisabledGroups(true);
      } else {
        setFormError({
          full_message: groupNamesStatus.error,
        });
      }
    }

    return () => {
      setGroups({ log_groups: [] });
    };
  }, [ groupNamesStatus.error ]);

  const handleSubmit = () => {
    console.log('Starting Kinesis auto-setup');
    createStreamFetch(ApiRoutes.INTEGRATIONS.AWS.KINESIS_AUTO_SETUP.CREATE_STREAM);
  };

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={handleSubmit}
                  buttonContent="Begin Automated Setup"
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
                          required/>

          <ValidatedInput id="awsCloudWatchAwsGroupName"
                          type="select"
                          fieldData={formData.awsCloudWatchAwsGroupName}
                          onChange={onChange}
                          label="CloudWatch Group Name"
                          required
                          disabled={groupNamesStatus.loading || disabledGroups}>

            {renderOptions(availableGroups, 'Choose CloudWatch Group', groupNamesStatus.loading)}
          </ValidatedInput>

          <FormAdvancedOptions onChange={onChange}/>

          {toggleSetup
          && <button onClick={toggleSetup} type="button">Choose Existing Kinesis Stream</button>}
        </FormWrap>
      </Col>
    </Row>
  );
};

KinesisSetup.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  toggleSetup: PropTypes.func,
};

KinesisSetup.defaultProps = {
  toggleSetup: null,
};

export default KinesisSetup;
