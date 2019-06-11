import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { Button, Col } from 'react-bootstrap';
import { Input } from 'components/bootstrap';

export default class Streams extends Component {
  static propTypes = {
    onSubmit: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired,
    getValue: PropTypes.func.isRequired,
  }

  render() {
    const { onChange, onSubmit, getValue } = this.props;

    return (
      <form onSubmit={onSubmit}>
        <Col md={9} mdOffset={6}>
          <h2>Choose Kinesis Stream</h2>
          <p>Below is a list of all the Streams we found configured within Kinesis. Please choose the Stream you would like us to parse, or follow the directions to begin <a href="/aws/cloudwatch/kinesis-setup">setting up your CloudWatch Group</a> to feed into a new Kinesis Stream.</p>

          <Input
            id="awsCloudWatchKinesisStream"
            type="select"
            value={getValue('awsCloudWatchKinesisStream')}
            onChange={onChange}
            label="Choose Stream"
            bsSize="large"
            required
          >
            <option value="">Choose Kinesis Stream</option>
            <option value="stream-name-1">Stream Name 1</option>
            <option value="stream-name-2">Stream Name 2</option>
            <option value="stream-name-3">Stream Name 3</option>
            <option value="stream-name-4">Stream Name 4</option>
          </Input>

          <Button type="submit">Verify Stream &amp; Format</Button>
        </Col>
      </form>
    )
  }
}
