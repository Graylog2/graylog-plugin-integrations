import React, { useContext, useState } from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';

import { Input } from 'components/bootstrap';
import { Button, ControlLabel } from 'components/graylog';
import { useTheme } from 'theme/GraylogThemeContext';

import { FormDataContext } from 'aws/context/FormData';
import KeySecret from './KeySecret';
import ARN from './ARN';
import Automatic from './Automatic';

const TYPE_AUTOMATIC = 'automatic';
const TYPE_KEYSECRET = 'key-secret';

const AWSAuthenticationTypes = ({ onChange }) => {
  const { colors } = useTheme();
  const { clearField, formData } = useContext(FormDataContext);
  const [showARN, setShowARN] = useState(false);
  const [currentType, setCurrenType] = useState(formData.awsAuthenticationType ? formData.awsAuthenticationType.value : 'automatic');

  const AuthWrapper = React.useCallback(styled.div`
    margin: 0 0 21px 9px;
    padding: 3px 0 3px 21px;
    border-left: 3px solid ${colors.secondary.tre};
  `, []);

  const isType = (type) => {
    return currentType === type;
  };

  const handleTypeChange = (e) => {
    setCurrenType(e.target.value);
    onChange({ target: { name: 'awsAuthenticationType', value: e.target.value } });

    if (isType(TYPE_AUTOMATIC)) {
      clearField('awsCloudWatchAwsKey');
      clearField('awsCloudWatchAwsSecret');
    }
  };

  const toggleShowARN = () => {
    if (showARN) {
      clearField('awsCloudWatchAssumeARN');
    }

    setShowARN(!showARN);
  };

  const ToggleLabel = React.useMemo(() => (
    <ControlLabel>
      AWS Authentication Type
      <Button bsStyle="link" bsSize="sm" onClick={toggleShowARN}>
        {showARN ? 'Clear' : 'Use'} Assumed Role (ARN)
      </Button>
    </ControlLabel>
  ));

  return (
    <>
      <Input type="select"
             name="awsAuthType"
             id="awsAuthType"
             onChange={handleTypeChange}
             label={ToggleLabel}
             defaultValue={currentType}>
        <option value="automatic">Automatic</option>
        <option value="key-secret">Key &amp; Secret</option>
      </Input>

      <AuthWrapper>
        {showARN && (
          <ARN awsARN={formData.awsCloudWatchAssumeARN} onChange={onChange} />
        )}

        {isType(TYPE_AUTOMATIC) && <Automatic />}

        {isType(TYPE_KEYSECRET) && (
          <KeySecret awsKey={formData.awsCloudWatchAwsKey}
                     awsSecret={formData.awsCloudWatchAwsSecret}
                     onChange={onChange} />
        )}
      </AuthWrapper>
    </>
  );
};


AWSAuthenticationTypes.propTypes = {
  onChange: PropTypes.func.isRequired,
};

export default AWSAuthenticationTypes;
