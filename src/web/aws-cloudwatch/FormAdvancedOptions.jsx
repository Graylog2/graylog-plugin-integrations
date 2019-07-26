import React, { useContext } from 'react';
import styled from 'styled-components';
import PropTypes from 'prop-types';
import { ControlLabel, FormGroup, InputGroup, FormControl } from 'react-bootstrap';
import { Input } from 'components/bootstrap';

import { FormDataContext } from './context/FormData';
import { AdvancedOptionsContext } from './context/AdvancedOptions';

const FormAdvancedOptions = ({ onChange }) => {
  const { formData } = useContext(FormDataContext);
  const { isAdvancedOptionsVisible, setAdvancedOptionsVisibility } = useContext(AdvancedOptionsContext);

  const {
    awsCloudWatchGlobalInput,
    awsCloudWatchAssumeARN,
    awsCloudWatchBatchSize,
    awsCloudWatchThrottleEnabled,
    awsCloudWatchThrottleWait,
  } = formData;

  const handleToggle = () => {
    setAdvancedOptionsVisibility(!isAdvancedOptionsVisible);
  };

  const throttlingEnabled = !!awsCloudWatchThrottleEnabled.value;

  return (
    <>
      <ToggleAdvancedOptions onClick={handleToggle} type="button">
          Advanced Options <i className="fa fa-angle-right fa-sm" />
      </ToggleAdvancedOptions>

      <AdvancedOptionsContent visible={isAdvancedOptionsVisible}>
        <Input id="awsCloudWatchGlobalInput"
               type="checkbox"
               value="global-input"
               defaultChecked={awsCloudWatchGlobalInput ? awsCloudWatchGlobalInput.value : ''}
               onChange={onChange}
               label="Global Input" />

        <Input id="awsCloudWatchAssumeARN"
               type="text"
               value={awsCloudWatchAssumeARN ? awsCloudWatchAssumeARN.value : ''}
               onChange={onChange}
               label="AWS assume role ARN" />

        <Input id="awsCloudWatchBatchSize"
               type="number"
               value={awsCloudWatchBatchSize.value || awsCloudWatchBatchSize.defaultValue}
               onChange={onChange}
               label="Kinesis Record batch size" />

        <FormGroup>
          <ControlLabel>Enable Throttling</ControlLabel>
          <InputGroup>
            <InputGroup.Addon>
              <input id="awsCloudWatchThrottleEnabled"
                     type="checkbox"
                     value="throttle-enabled"
                     checked={throttlingEnabled}
                     onChange={onChange} />
            </InputGroup.Addon>
            <FormControl id="awsCloudWatchThrottleWait"
                         type="number"
                         value={throttlingEnabled ? awsCloudWatchThrottleWait.value || awsCloudWatchThrottleWait.defaultValue : awsCloudWatchThrottleWait.defaultValue}
                         onChange={onChange}
                         disabled={!throttlingEnabled} />
            <InputGroup.Addon>msgs per second</InputGroup.Addon>
          </InputGroup>
        </FormGroup>

      </AdvancedOptionsContent>
    </>
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
  display: block;
  margin: 0 0 35px;

  :hover {
    color: #5e123b;
    text-decoration: underline;
  }
`;

export default FormAdvancedOptions;
