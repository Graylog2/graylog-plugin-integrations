import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import { Input } from 'components/bootstrap';

import FormWrap from '../common/FormWrap';

import { FormDataContext } from './providers/FormData';
import FormAdvancedOptions from './FormAdvancedOptions';
import Routes from '../common/Routes';

const KinesisStreams = ({ onChange, onSubmit, isAdvancedOptionsVisible, setAdvancedOptionsVisiblity }) => {
  const { formData } = useContext(FormDataContext);

  return (
    <Row>
      <Col md={8}>
        <FormWrap onSubmit={onSubmit} buttonContent="Verify Stream &amp; Format">
          <h2>Choose Kinesis Stream</h2>
          <p>Below is a list of all Kinesis Streams found within the specified AWS account. Please choose the Stream you would like us to read messages from, or follow the directions to begin <a href={Routes.INTEGRATIONS.AWS.CLOUDWATCH.step('kinesis-setup')}>setting up your CloudWatch Log Group</a> to feed messages into a new Kinesis Stream.</p>

          <Input id="awsCloudWatchKinesisStream"
                 type="select"
                 value={formData.awsCloudWatchKinesisStream ? formData.awsCloudWatchKinesisStream.value : ''}
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
                               toggle={setAdvancedOptionsVisiblity}
                               visible={isAdvancedOptionsVisible} />
        </FormWrap>
      </Col>
    </Row>
  );
};

KinesisStreams.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  isAdvancedOptionsVisible: PropTypes.bool.isRequired,
  setAdvancedOptionsVisiblity: PropTypes.func.isRequired,
};

export default KinesisStreams;
