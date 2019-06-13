import React from 'react';
import styled from '@emotion/styled';
import PropTypes from 'prop-types';
import { Input } from 'components/bootstrap';

const FormAdvancedOptions = ({ onChange, getValue, toggle, opened }) => {
  return (
    <div>
      <ToggleAdvancedOptions onClick={toggle} type="button">
          Advanced Options <i className="fa fa-angle-right fa-sm" />
      </ToggleAdvancedOptions>

      <AdvancedOptionsContent opened={opened}>
        <Input id="awsCloudWatchGlobalInput"
               type="checkbox"
               value="1"
               defaultChecked={getValue('awsCloudWatchGlobalInput')}
               onChange={onChange}
               label="Global Input" />

        <Input id="awsCloudWatchAssumeARN"
               type="text"
               value={getValue('awsCloudWatchAssumeARN')}
               onChange={onChange}
               label="AWS assume role ARN" />

        <Input id="awsCloudWatchBatchSize"
               type="number"
               value={getValue('awsCloudWatchBatchSize')}
               onChange={onChange}
               label="Kinesis Record batch size" />

        <Input id="awsCloudWatchThrottleEnabled"
               type="checkbox"
               value="1"
               defaultChecked={getValue('awsCloudWatchThrottleEnabled')}
               onChange={onChange}
               label="Enable Throttle" />

        <Input id="awsCloudWatchThrottleWait"
               type="number"
               value={getValue('awsCloudWatchThrottleWait')}
               onChange={onChange}
               label="Throttled wait milliseconds" />
      </AdvancedOptionsContent>
    </div>
  );
};

FormAdvancedOptions.propTypes = {
  onChange: PropTypes.func.isRequired,
  getValue: PropTypes.func.isRequired,
  toggle: PropTypes.func.isRequired,
  opened: PropTypes.bool.isRequired,
};

const AdvancedOptionsContent = styled.div`
  display: ${props => (props.opened ? 'block' : 'none')};

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
