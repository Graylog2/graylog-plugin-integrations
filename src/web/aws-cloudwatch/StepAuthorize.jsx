import React from 'react';
import PropTypes from 'prop-types';
import { Button, Col, Row } from 'react-bootstrap';

import { Input } from 'components/bootstrap';

const StepAuthorize = ({ onChange, onSubmit, getValue }) => {
  return (
    <Row>
      <Col md={8}>
        <form onSubmit={onSubmit}>
          <h2>Create Integration &amp; Authorize AWS</h2>
          <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ipsum facere quis maiores doloribus asperiores modi dignissimos enim accusamus sunt aliquid, pariatur eligendi esse dolore temporibus corporis corrupti dolorum, soluta consectetur?</p>

          <Input id="awsCloudWatchName"
                 type="text"
                 value={getValue('awsCloudWatchName')}
                 onChange={onChange}
                 placeholder="CloudWatch Integration Name"
                 label="Name"
                 required />

          <Input id="awsCloudWatchDescription"
                 type="textarea"
                 label="Description"
                 placeholder="CloudWatch Integration Description"
                 onChange={onChange}
                 value={getValue('awsCloudWatchDescription')} />

          <Input id="awsCloudWatchAwsKey"
                 type="text"
                 label="AWS Key"
                 placeholder="CloudWatch Integration AWS Key"
                 onChange={onChange}
                 value={getValue('awsCloudWatchAwsKey')}
                 autoComplete="off"
                 required />

          <Input id="awsCloudWatchAwsSecret"
                 type="password"
                 label="AWS Secret"
                 placeholder="CloudWatch Integration AWS Secret"
                 onChange={onChange}
                 value={getValue('awsCloudWatchAwsSecret')}
                 autoComplete="off"
                 required />

          <Input id="awsCloudWatchAwsRegion"
                 type="select"
                 value={getValue('awsCloudWatchAwsRegion')}
                 onChange={onChange}
                 label="Region"
                 required>
            <option value="">Choose Region</option>
            <option value="us-east-2">US East (Ohio)</option>
            <option value="us-east-1">US East (N. Virginia)</option>
            <option value="us-west-1">US West (N. California)</option>
            <option value="us-west-2">US West (Oregon)</option>
          </Input>

          <Button type="submit">Authorize &amp; Choose Stream</Button>
        </form>
      </Col>
    </Row>
  );
};

StepAuthorize.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  getValue: PropTypes.func.isRequired,
};

export default StepAuthorize;
