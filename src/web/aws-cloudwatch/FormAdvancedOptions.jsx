import React, { Component } from 'react'
import styled from '@emotion/styled';
import PropTypes from 'prop-types'
import { Input } from 'components/bootstrap';

export default class FormAdvancedOptions extends Component {
  static propTypes = {
    onChange: PropTypes.func.isRequired,
    getValue: PropTypes.func.isRequired,
  }

  state = {
    opened: false,
    throttled: false,
  }

  toggleOptions = () => {
    const { opened } = this.state;

    this.setState({
      opened: !opened,
    });
  }

  toggleCheckbox = (name) => ({ target }) => {
    const { onChange } = this.props;

    onChange({ target: { id: name, value: target.checked ? target.value : '' }});

    this.setState({
      throttled: target.checked,
    })
  }

  render() {
    const { onChange, getValue } = this.props;
    const { opened } = this.state;

    return (
      <div>
        <ToggleAdvancedOptions onClick={this.toggleOptions} type="button">
          Advanced Options <i className="fa fa-angle-right fa-sm" />
        </ToggleAdvancedOptions>

        <AdvancedOptionsContent opened={opened}>
          <Input
            id="awsCloudWatchGlobalInput"
            type="checkbox"
            value={'1'}
            defaultChecked={getValue('awsCloudWatchGlobalInput')}
            onChange={this.toggleCheckbox('awsCloudWatchGlobalInput')}
            label="Global Input"
          />

          <Input
            id="awsCloudWatchAssumeARN"
            type="text"
            value={getValue('awsCloudWatchAssumeARN')}
            onChange={onChange}
            label="AWS assume role ARN"
          />

          <Input
            id="awsCloudWatchBatchSize"
            type="number"
            value={getValue('awsCloudWatchBatchSize')}
            onChange={onChange}
            label="Kinesis Record batch size"
          />

          <Input
            id="awsCloudWatchThrottleEnabled"
            type="checkbox"
            value={'1'}
            defaultChecked={getValue('awsCloudWatchThrottleEnabled')}
            onChange={this.toggleCheckbox('awsCloudWatchThrottleEnabled')}
            label="Enable Throttle"
          />

          <Input
            id="awsCloudWatchThrottleWait"
            type="number"
            value={getValue('awsCloudWatchThrottleWait')}
            onChange={onChange}
            label="Throttled wait milliseconds"
          />
        </AdvancedOptionsContent>
      </div>
    )
  }
}

const AdvancedOptionsContent = styled.div`
  display: ${props => props.opened ? 'block' : 'none'};

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
