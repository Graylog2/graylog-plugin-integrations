import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import { Input } from 'components/bootstrap';

import FormWrap from '../common/FormWrap';

import FormAdvancedOptions from './FormAdvancedOptions';
import { FormDataContext } from './context/FormData';

const KinesisSetup = ({ onChange, onSubmit }) => {
  const { formData } = useContext(FormDataContext);

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={onSubmit} buttonContent="Verify &amp; Format">
          <h2>Create Kinesis Stream</h2>
          <p>We&apos;re going to get started setting up your Kinesis Stream, just give us a name and choose the related CloudWatch Group. We&apos;ll handle the hard stuff!</p>

          <Input id="awsCloudWatchKinesisStream"
                 type="text"
                 label="Kinesis Stream Name"
                 placeholder="Create Stream Name"
                 onChange={onChange}
                 defaultValue={formData.awsCloudWatchKinesisStream ? formData.awsCloudWatchKinesisStream.value : ''}
                 required />

          <Input id="awsCloudWatchAwsGroupName"
                 type="select"
                 value={formData.awsCloudWatchAwsGroupName ? formData.awsCloudWatchAwsGroupName.value : ''}
                 onChange={onChange}
                 label="CloudWatch Group Name"
                 required>
            <option value="">Choose CloudWatch Group</option>
            <option value="group-name-1">Group Name 1</option>
            <option value="group-name-2">Group Name 2</option>
            <option value="group-name-3">Group Name 3</option>
            <option value="group-name-4">Group Name 4</option>
          </Input>

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
