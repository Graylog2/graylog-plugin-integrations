import React, { useContext } from 'react';
import PropTypes from 'prop-types';

import { FormDataContext } from 'aws/context/FormData';
import KeySecret from './KeySecret';
import ARN from './ARN';
import Automatic from './Automatic';


const AWSAuthenticationTypes = ({ onChange }) => {
  const { formData } = useContext(FormDataContext);

  return (
    <>
      <KeySecret awsKey={formData.awsCloudWatchAwsKey}
                 awsSecret={formData.awsCloudWatchAwsSecret}
                 onChange={onChange} />

      <ARN awsARN={formData.awsCloudWatchAwsKey}
           onChange={onChange} />

      <Automatic />
    </>
  );
};

AWSAuthenticationTypes.propTypes = {
  onChange: PropTypes.func.isRequired,
};

export default AWSAuthenticationTypes;
