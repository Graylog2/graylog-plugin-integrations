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
      <ToggleAdvancedOptions onClick={handleToggle} type="button" visible={isAdvancedOptionsVisible}>
        {isAdvancedOptionsVisible ? 'Close' : 'Open'} Advanced Options <i className="fa fa-angle-right fa-sm" />
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
  background-color: #DCE1E5;
  padding: 15px;
  border: 1px solid #A6AFBD;
  border-top: 0;
  border-radius: 0 0 4px 4px;
  margin-bottom: 15px;
`;

const ToggleAdvancedOptions = styled.button`
  color: #0063BE;
  font-size: 14px;
  border-radius: 4px 4px 0 0;
  border-bottom-right-radius: ${props => (props.visible ? '0' : '4px')};
  border-bottom-left-radius: ${props => (props.visible ? '0' : '4px')};
  border: 1px solid #A6AFBD;
  border-bottom-width: ${props => (props.visible ? '0' : '1px')};
  padding: 6px 15px;
  background-color: #DCE1E5;
  display: block;
  width: 100%;
  margin-bottom: ${props => (props.visible ? '0' : '15px')};

  :hover {
    color: #5e123b;
    text-decoration: underline;
  }
`;

export default FormAdvancedOptions;
