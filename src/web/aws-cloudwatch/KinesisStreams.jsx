import React from 'react';
import PropTypes from 'prop-types';
import { Button, Col, Row } from 'react-bootstrap';

import { Input } from 'components/bootstrap';
import FormAdvancedOptions from './FormAdvancedOptions';
import ROUTES from '../common/Routes';

const KinesisStreams = ({ onChange, onSubmit, values, toggleAdvancedOptions, visibleAdvancedOptions }) => {
  return (
    <Row>
      <Col md={8}>
        <form onSubmit={onSubmit}>
          <h2>Choose Kinesis Stream</h2>
          <p>Below is a list of all the Streams we found configured within Kinesis. Please choose the Stream you would like us to parse, or follow the directions to begin <a href={ROUTES.INTEGRATIONS.CLOUDWATCH_STEP('kinesis-setup')}>setting up your CloudWatch Group</a> to feed into a new Kinesis Stream.</p>

          <Input id="awsCloudWatchKinesisStream"
                 type="select"
                 value={values.awsCloudWatchKinesisStream}
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
                               values={values}
                               toggle={toggleAdvancedOptions}
                               visible={visibleAdvancedOptions} />

          <Button type="submit" bsStyle="primary">Verify Stream &amp; Format</Button>
        </form>
      </Col>
    </Row>
  );
};

KinesisStreams.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  values: PropTypes.object.isRequired,
  toggleAdvancedOptions: PropTypes.func.isRequired,
  visibleAdvancedOptions: PropTypes.bool.isRequired,
};

export default KinesisStreams;
