import React from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import { Input } from 'components/bootstrap';

import FormWrap from '../common/FormWrap';

import formDataHook from './hooks/formData';
import advancedOptionsHook from './hooks/advancedOptions';
import FormAdvancedOptions from './FormAdvancedOptions';
import Routes from '../common/Routes';

const KinesisStreams = ({ onChange, onSubmit, toggleAdvancedOptions }) => {
  const { getFieldValue } = formDataHook();
  const { getAdvancedOptionsVisiblity } = advancedOptionsHook();

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={onSubmit} buttonContent="Verify Stream &amp; Format">
          <h2>Choose Kinesis Stream</h2>
          <p>Below is a list of all Kinesis Streams found within the specified AWS account. Please choose the Stream you would like us to read messages from, or follow the directions to begin <a href={Routes.INTEGRATIONS.AWS.CLOUDWATCH.step('kinesis-setup')}>setting up your CloudWatch Log Group</a> to feed messages into a new Kinesis Stream.</p>

          <Input id="awsCloudWatchKinesisStream"
                 type="select"
                 value={getFieldValue('awsCloudWatchKinesisStream')}
                 onChange={onChange}
                 label="Choose Stream"
                 required>
            <option value="">Choose Kinesis Stream</option>
            <option value="stream-name-1">Stream Name 1</option>
            <option value="stream-name-2">Stream Name 2</option>
            <option value="stream-name-3">Stream Name 3</option>
            <option value="stream-name-4">Stream Name 4</option>
          </Input>

          <FormAdvancedOptions onChange={onChange}
                               toggle={toggleAdvancedOptions}
                               visible={getAdvancedOptionsVisiblity()} />
        </FormWrap>
      </Col>
    </Row>
  );
};

KinesisStreams.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  toggleAdvancedOptions: PropTypes.func.isRequired,
};

export default KinesisStreams;
