import React, { useContext } from 'react';
import styled from '@emotion/styled';
import PropTypes from 'prop-types';
import { Input } from 'components/bootstrap';

import { FormDataContext } from './context/FormData';
import { AdvancedOptionsContext } from './context/AdvancedOptions';

const FormAdvancedOptions = ({ onChange }) => {
  const { formData } = useContext(FormDataContext);
  const { isAdvancedOptionsVisible, setAdvancedOptionsVisibility } = useContext(AdvancedOptionsContext);

  const handleToggle = () => {
    setAdvancedOptionsVisibility(!isAdvancedOptionsVisible);
  };

  return (
    <div>
      <ToggleAdvancedOptions onClick={handleToggle} type="button">
          Advanced Options <i className="fa fa-angle-right fa-sm" />
      </ToggleAdvancedOptions>

      <AdvancedOptionsContent visible={isAdvancedOptionsVisible}>
        <Input id="awsCloudWatchGlobalInput"
               type="checkbox"
               value="global-input"
               defaultChecked={formData.awsCloudWatchGlobalInput ? formData.awsCloudWatchGlobalInput.value : ''}
               onChange={onChange}
               label="Global Input" />

        <Input id="awsCloudWatchAssumeARN"
               type="text"
               value={formData.awsCloudWatchAssumeARN ? formData.awsCloudWatchAssumeARN.value : ''}
               onChange={onChange}
               label="AWS assume role ARN" />

        <Input id="awsCloudWatchBatchSize"
               type="number"
               value={formData.awsCloudWatchBatchSize.value || formData.awsCloudWatchBatchSize.defaultValue}
               onChange={onChange}
               label="Kinesis Record batch size" />

        <Input id="awsCloudWatchThrottleEnabled"
               type="checkbox"
               value="throttle-enabled"
               defaultChecked={formData.awsCloudWatchThrottleEnabled ? formData.awsCloudWatchThrottleEnabled.value : ''}
               onChange={onChange}
               label="Enable Throttle" />

        <Input id="awsCloudWatchThrottleWait"
               type="number"
               value={formData.awsCloudWatchThrottleWait.value || formData.awsCloudWatchThrottleWait.defaultValue}
               onChange={onChange}
               label="Throttled wait milliseconds" />
      </AdvancedOptionsContent>
    </div>
  );
};

FormAdvancedOptions.propTypes = {
  onChange: PropTypes.func.isRequired,
};

const AdvancedOptionsContent = styled.div`
  display: ${props => (props.visible ? 'block' : 'none')};
`;

const ToggleAdvancedOptions = styled.button`
  border: 0;
  color: #16ace3;
  font-size: 14px;

  :hover {
    color: #5e123b;
    text-decoration: underline;
  }
`;

export default FormAdvancedOptions;
