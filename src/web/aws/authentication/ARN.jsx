import React from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';

import { Input } from 'components/bootstrap';

const ARN = ({ awsARN, onChange }) => {
  return (
    <Input id="awsCloudWatchAssumeARN"
           type="text"
           value={awsARN.value}
           onChange={onChange}
           label={['AWS Assume Role (ARN) ', <Optional>Optional</Optional>]}
           help="Amazon Resource Name with required cross account permission"
           placeholder="arn:aws:sts::123456789012:assumed-role/some-role"
           maxLength="2048" />
  );
};

const Optional = styled.small`
  font-weight: 400;
  font-style: italic;
`;

ARN.propTypes = {
  awsARN: PropTypes.string,
  onChange: PropTypes.func.isRequired,
};

ARN.defaultProps = {
  awsARN: '',
};

export default ARN;
