import React from 'react';
import PropTypes from 'prop-types';

import { Input } from 'components/bootstrap';

const ARN = ({ awsARN, onChange }) => {
  return (
    <Input id="awsCloudWatchAssumeARN"
           type="text"
           value={awsARN}
           onChange={onChange}
           label="AWS Amazon Resource Name (ARN)"
           help="Role ARN with required permissions (cross account access)" />
  );
};

ARN.propTypes = {
  awsARN: PropTypes.string,
  onChange: PropTypes.func.isRequired,
};

ARN.defaultProps = {
  awsARN: '',
};

export default ARN;
