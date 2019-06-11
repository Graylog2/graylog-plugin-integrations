import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Button, Col } from 'react-bootstrap';
import { Input } from 'components/bootstrap';

export default class Authorize extends Component {
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
          <h2>Create Integration &amp; Authorize AWS</h2>
          <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ipsum facere quis maiores doloribus asperiores modi dignissimos enim accusamus sunt aliquid, pariatur eligendi esse dolore temporibus corporis corrupti dolorum, soluta consectetur?</p>

          <Input
            id="awsCloudWatchName"
            type="text"
            value={getValue('awsCloudWatchName')}
            onChange={onChange}
            placeholder="CloudWatch Integration Name"
            bsSize="large"
            label="Name"
            required
          />

          <Input
            id="awsCloudWatchDescription"
            type="textarea"
            label="Description"
            placeholder="CloudWatch Integration Description"
            bsSize="large"
            onChange={onChange}
            value={getValue('awsCloudWatchDescription')}
          />

          <Input
            id="awsCloudWatchAwsKey"
            type="text"
            label="AWS Key"
            placeholder="CloudWatch Integration AWS Key"
            bsSize="large"
            onChange={onChange}
            value={getValue('awsCloudWatchAwsKey')}
            required
          />

          <Input
            id="awsCloudWatchAwsSecret"
            type="password"
            label="AWS Secret"
            placeholder="CloudWatch Integration AWS Secret"
            bsSize="large"
            onChange={onChange}
            value={getValue('awsCloudWatchAwsSecret')}
            required
          />

          <Input
            id="awsCloudWatchAwsRegion"
            type="select"
            value={getValue('awsCloudWatchAwsRegion')}
            onChange={onChange}
            label="Region"
            bsSize="large"
            required
          >
            <option value="">Choose Region</option>
            <option value="us-east-2">US East (Ohio)</option>
            <option value="us-east-1">US East (N. Virginia)</option>
            <option value="us-west-1">US West (N. California)</option>
            <option value="us-west-2">US West (Oregon)</option>
          </Input>

          <Button type="submit">Authorize &amp; Choose Stream</Button>
        </Col>
      </form>
    )
  }
}
