import React from 'react';
import PropTypes from 'prop-types';
import { Panel } from 'react-bootstrap';

const Agree = ({ onSubmit, groupName, streamName }) => {
  return (
    <Panel bsStyle="warning" header={<h2>Kinesis Auto Setup</h2>}>
      <p>This auto setup will create the following AWS resources. Click below to acknowledge that you understand
              that these resources will be created and that you are solely responsible for any associated AWS fees
              incurred
              from them. Note that all resources must be manually deleted by you if they are not needed.
      </p>

      <ol>
        <li>Create a Kinesis Stream with [1] shard.</li>
        <li>Create an IAM Role and Policy to allow the specified CloudWatch group [{groupName}] to publish log messages to the Kinesis stream [{streamName}]</li>
        <li>Create a CloudWatch Subscription, which publishes log messages to the Kinesis stream.</li>
      </ol>

      <button onClick={onSubmit}
              type="button"
              className="btn btn-success">
          I Agree! Create these AWS resources now.
      </button>
    </Panel>
  );
};

Agree.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  groupName: PropTypes.string.isRequired,
  streamName: PropTypes.string.isRequired,
};

export default Agree;
