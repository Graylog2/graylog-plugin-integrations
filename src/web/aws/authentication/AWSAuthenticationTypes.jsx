import React, { useContext, useState } from 'react';
import PropTypes from 'prop-types';

import { Input } from 'components/bootstrap';

import { FormDataContext } from 'aws/context/FormData';
import KeySecret from './KeySecret';
import ARN from './ARN';
import Automatic from './Automatic';

const TYPE_AUTOMATIC = 'automatic';
const TYPE_KEYSECRET = 'key-secret';
const TYPE_ARN = 'arn';

const AWSAuthenticationTypes = ({ onChange }) => {
  const { clearField, formData } = useContext(FormDataContext);
  const [currentType, setCurrenType] = useState(formData.awsAuthenticationType ? formData.awsAuthenticationType.value : 'automatic');

  const isType = (type) => {
    return currentType === type;
  };

  const handleTypeChange = (e) => {
    setCurrenType(e.target.value);
    onChange({ target: { name: 'awsAuthenticationType', value: e.target.value } });

    if (isType(TYPE_AUTOMATIC) || isType(TYPE_ARN)) {
      clearField('awsCloudWatchAwsKey');
      clearField('awsCloudWatchAwsSecret');
    }

    if (isType(TYPE_AUTOMATIC) || isType(TYPE_KEYSECRET)) {
      clearField('awsCloudWatchAssumeARN');
    }
  };

  return (
    <>
      <Input type="select"
             name="awsAuthType"
             id="awsAuthType"
             onChange={handleTypeChange}
             label="AWS Authentication Type"
             defaultValue={currentType}>
        <option value="automatic">Automatic</option>
        <option value="key-secret">Key &amp; Secret</option>
        <option value="arn">ARN - Amazon Resource Name</option>
      </Input>

      {isType(TYPE_KEYSECRET) && (
      <KeySecret awsKey={formData.awsCloudWatchAwsKey}
                 awsSecret={formData.awsCloudWatchAwsSecret}
                 onChange={onChange} />
      )}

      {isType(TYPE_ARN) && (
      <ARN awsARN={formData.awsCloudWatchAssumeARN}
           onChange={onChange} />
      )}

      {isType(TYPE_AUTOMATIC) && <Automatic />}
    </>
  );
};

AWSAuthenticationTypes.propTypes = {
  onChange: PropTypes.func.isRequired,
};

export default AWSAuthenticationTypes;