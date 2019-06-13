import React from 'react';
import PropTypes from 'prop-types';
import { Button, Col, Row } from 'react-bootstrap';

import { Input } from 'components/bootstrap';

const KinesisSetup = ({ getValue, onChange, onSubmit }) => {
  return (
    <Row>
      <Col md={8}>
        <form onSubmit={onSubmit}>
          <h2>Create Kinesis Stream</h2>
          <p>We&apos;re going to get started setting up your Kinesis Stream, just give us a name and choose the related CloudWatch Group. We&apos;ll handle the hard stuff!</p>

          <Input id="awsCloudWatchKinesisStream"
                 type="text"
                 label="Kinesis Stream Name"
                 placeholder="Create Stream Name"
                 onChange={onChange}
                 value={getValue('awsCloudWatchKinesisStream')}
                 required />

          <Input id="awsCloudWatchAwsGroupName"
                 type="select"
                 value={getValue('awsCloudWatchAwsGroupName')}
                 onChange={onChange}
                 label="CloudWatch Group Name"
                 required>
            <option value="">Choose CloudWatch Group</option>
            <option value="group-name-1">Group Name 1</option>
            <option value="group-name-2">Group Name 2</option>
            <option value="group-name-3">Group Name 3</option>
            <option value="group-name-4">Group Name 4</option>
          </Input>

          <Button type="submit">Verify &amp; Format</Button>
        </form>
      </Col>
    </Row>
  );
};

KinesisSetup.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  getValue: PropTypes.func.isRequired,
};

export default KinesisSetup;
