import React from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import StyledForm from '../common/StyledForm';
import StyledInput from '../common/StyledInput';

import FormAdvancedOptions from './FormAdvancedOptions';
import Routes from '../common/Routes';

const KinesisStreams = ({ onChange, onSubmit, formErrors, values, toggleAdvancedOptions, visibleAdvancedOptions }) => {
  return (
    <Row>
      <Col>
        <StyledForm onSubmit={onSubmit} buttonContent="Verify Stream &amp; Format">
          <h2>Choose Kinesis Stream</h2>
          <p>Below is a list of all Kinesis Streams found within the specified AWS account. Please choose the Stream you would like us to read messages from, or follow the directions to begin <a href={Routes.INTEGRATIONS.AWS.CLOUDWATCH.step('kinesis-setup')}>setting up your CloudWatch Log Group</a> to feed messages into a new Kinesis Stream.</p>

          <StyledInput id="awsCloudWatchKinesisStream"
                       type="select"
                       value={values.awsCloudWatchKinesisStream}
                       onChange={onChange}
                       label="Choose Stream"
                       hasError={formErrors.awsCloudWatchKinesisStream}
                       required>
            <option value="">Choose Kinesis Stream</option>
            <option value="stream-name-1">Stream Name 1</option>
            <option value="stream-name-2">Stream Name 2</option>
            <option value="stream-name-3">Stream Name 3</option>
            <option value="stream-name-4">Stream Name 4</option>
          </StyledInput>

          <FormAdvancedOptions onChange={onChange}
                               values={values}
                               toggle={toggleAdvancedOptions}
                               visible={visibleAdvancedOptions} />
        </StyledForm>
      </Col>
    </Row>
  );
};

KinesisStreams.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  values: PropTypes.array.isRequired,
  toggleAdvancedOptions: PropTypes.func.isRequired,
  visibleAdvancedOptions: PropTypes.bool.isRequired,
  formErrors: PropTypes.object,
};

KinesisStreams.defaultProps = {
  formErrors: {},
};

export default KinesisStreams;
