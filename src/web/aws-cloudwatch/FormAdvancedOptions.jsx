import React from 'react';
import styled from '@emotion/styled';
import PropTypes from 'prop-types';
import { Input } from 'components/bootstrap';

const FormAdvancedOptions = ({ onChange, values, toggle, visible }) => {
  return (
    <div>
      <ToggleAdvancedOptions onClick={toggle} type="button">
          Advanced Options <i className="fa fa-angle-right fa-sm" />
      </ToggleAdvancedOptions>

      <AdvancedOptionsContent visible={visible}>
        <Input id="awsCloudWatchGlobalInput"
               type="checkbox"
               value="1"
               defaultChecked={values.awsCloudWatchGlobalInput}
               onChange={onChange}
               label="Global Input" />

        <Input id="awsCloudWatchAssumeARN"
               type="text"
               value={values.awsCloudWatchAssumeARN}
               onChange={onChange}
               label="AWS assume role ARN" />

        <Input id="awsCloudWatchBatchSize"
               type="number"
               value={values.awsCloudWatchBatchSize}
               onChange={onChange}
               label="Kinesis Record batch size" />

        <Input id="awsCloudWatchThrottleEnabled"
               type="checkbox"
               value="1"
               defaultChecked={values.awsCloudWatchThrottleEnabled}
               onChange={onChange}
               label="Enable Throttle" />

        <Input id="awsCloudWatchThrottleWait"
               type="number"
               value={values.awsCloudWatchThrottleWait}
               onChange={onChange}
               label="Throttled wait milliseconds" />
      </AdvancedOptionsContent>
    </div>
  );
};

FormAdvancedOptions.propTypes = {
  onChange: PropTypes.func.isRequired,
  values: PropTypes.object.isRequired,
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
