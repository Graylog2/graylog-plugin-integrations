import React from 'react';
import styled from '@emotion/styled';
import PropTypes from 'prop-types';
import { Input } from 'components/bootstrap';

import formDataHook from './hooks/formData';

const FormAdvancedOptions = ({ onChange, toggle, visible }) => {
  const { getFieldValue } = formDataHook();

  return (
    <div>
      <ToggleAdvancedOptions onClick={toggle} type="button">
          Advanced Options <i className="fa fa-angle-right fa-sm" />
      </ToggleAdvancedOptions>

      <AdvancedOptionsContent visible={visible}>
        <Input id="awsCloudWatchGlobalInput"
               type="checkbox"
               value="global-input"
               defaultChecked={getFieldValue('awsCloudWatchGlobalInput')}
               onChange={onChange}
               label="Global Input" />

        <Input id="awsCloudWatchAssumeARN"
               type="text"
               value={getFieldValue('awsCloudWatchAssumeARN')}
               onChange={onChange}
               label="AWS assume role ARN" />

        <Input id="awsCloudWatchBatchSize"
               type="number"
               value={getFieldValue('awsCloudWatchBatchSize')}
               onChange={onChange}
               label="Kinesis Record batch size" />

        <Input id="awsCloudWatchThrottleEnabled"
               type="checkbox"
               value="throttle-enabled"
               defaultChecked={getFieldValue('awsCloudWatchThrottleEnabled')}
               onChange={onChange}
               label="Enable Throttle" />

        <Input id="awsCloudWatchThrottleWait"
               type="number"
               value={getFieldValue('awsCloudWatchThrottleWait')}
               onChange={onChange}
               label="Throttled wait milliseconds" />
      </AdvancedOptionsContent>
    </div>
  );
};

FormAdvancedOptions.propTypes = {
  onChange: PropTypes.func.isRequired,
  toggle: PropTypes.func.isRequired,
  visible: PropTypes.bool.isRequired,
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
