import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { Button, Col } from 'react-bootstrap';
import { Input } from 'components/bootstrap';

export default class KinesisSetup extends Component {
  static propTypes = {
    onSubmit: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired,
    getValue: PropTypes.func.isRequired,
  }

  render() {
    const { getValue, onChange, onSubmit } = this.props;

    return (
      <form onSubmit={onSubmit}>
        <Col md={9} mdOffset={6}>
          <h2>Create Kinesis Stream</h2>
          <p>We're going to get started setting up your Kinesis Stream, just give us a name and choose the related CloudWatch Group. We'll handle the hard stuff!</p>

          <Input
            id="awsCloudWatchKinesisStream"
            type="text"
            label="Kinesis Stream Name"
            placeholder="Create Stream Name"
            bsSize="large"
            onChange={onChange}
            value={getValue('awsCloudWatchKinesisStream')}
            required
          />

          <Input
            id="awsCloudWatchAwsGroupName"
            type="select"
            value={getValue('awsCloudWatchAwsGroupName')}
            onChange={onChange}
            label="CloudWatch Group Name"
            bsSize="large"
            required
          >
            <option value="">Choose CloudWatch Group</option>
            <option value="group-name-1">Group Name 1</option>
            <option value="group-name-2">Group Name 2</option>
            <option value="group-name-3">Group Name 3</option>
            <option value="group-name-4">Group Name 4</option>
          </Input>

          <Button type="submit">Verify &amp; Format</Button>
        </Col>
      </form>
    )
  }
}
