/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
import React from 'react';
import cloneDeep from 'lodash/cloneDeep';
import get from 'lodash/get';
import camelCase from 'lodash/camelCase';
import { getValueFromInput } from 'util/FormsUtils';
import { Input, Button, ControlLabel, FormControl, FormGroup, HelpBlock, InputGroup } from 'components/bootstrap';
import { ConfigType, ValidationType } from '../types';

type OpsGenieNotificationFormType = {
      config: ConfigType,
      validation: ValidationType
      onChange: any
}

class OpsGenieNotificationForm extends React.Component<OpsGenieNotificationFormType, any> {

    static defaultConfig = {
      channel: 'team-name',
      access_token: '',
      tags: "Graylog-alert",
      /* eslint-disable no-template-curly-in-string */
      main_fields: ''
                     + 'Title:       ${event_definition_title}\n'
                     + 'Type:        ${event_definition_type}\n'
                     + 'Priority:             ${event.priority}\n'
                     + 'Message:              ${event.message}\n',

      custom_message: ''
                      + 'Key:                  ${event.key}\n'
                      + 'Timestamp:            ${event.timestamp}\n'
                      + 'Source:               ${event.source}\n'
                      + 'Alert:                ${event.alert}\n'
                      + 'Timestamp Processing: ${event.timestamp}\n'
                      + 'Timerange Start:      ${event.timerange_start}\n'
                      + 'Timerange End:        ${event.timerange_end}\n'
                      + 'Event Fields:\n'
                      + '${foreach event.fields field}\n'
                      + '${field.key}: ${field.value}\n'
                      + '${end}\n'
                      + '${if backlog}\n'
                      + 'Last messages accounting for this alert:\n'
                      + '${foreach backlog message}\n'
                      + '${message.timestamp}  ::  ${message.source}  ::  ${message.message}\n'
                      + '${message.message}\n'
                      + '${end}'
                      + '${end}\n',
      /* eslint-enable no-template-curly-in-string */
      user_name: 'Graylog',
      backlog_size: 0,

    };
    constructor(props: OpsGenieNotificationFormType | Readonly<OpsGenieNotificationFormType>) {
      super(props);

      const defaultBacklogSize = props.config.backlog_size;

      this.state = {
        isBacklogSizeEnabled: defaultBacklogSize > 0,
        backlogSize: defaultBacklogSize,
      };
    }

    handleBacklogSizeChange = (event: { target: { name: string; }; }) => {
      const { name } = event.target;
      const value = getValueFromInput(event.target);

      this.setState({ [camelCase(name)]: value });
      this.propagateChange(name, getValueFromInput(event.target));
    };

    toggleBacklogSize = () => {
      const { isBacklogSizeEnabled, backlogSize } = this.state;

      this.setState({ isBacklogSizeEnabled: !isBacklogSizeEnabled });
      this.propagateChange('backlog_size', (isBacklogSizeEnabled ? 0 : backlogSize));
    };

    propagateChange = (key: string, value: any) => {
      const { config, onChange } = this.props;
      const nextConfig = cloneDeep(config);
      nextConfig[key] = value;
      onChange(nextConfig);
    };

    handleChange = (event: { target: { name: any; }; }) => {
      const { name } = event.target;
      this.propagateChange(name, getValueFromInput(event.target));
    };

    render() {
      const { config, validation } = this.props;
      const { isBacklogSizeEnabled, backlogSize } = this.state;
      const url = 'https://docs.graylog.org/en/latest/pages/alerts.html#data-available-to-notifications';
      const element = <p>Custom message to be appended below the alert details. See <a href={url} rel="noopener noreferrer" target="_blank">docs </a>for more details.</p>;
      const mainFieldElement = <p>OpsGenie fields to be mapped to the alert message. See <a href={url} rel="noopener noreferrer" target="_blank">docs </a>for more details.</p>;

      return (
        <>
          <Input id="notification-webhookUrl"
                 name="access_token"
                 label="API Access Token"
                 type="password"
                 bsStyle={validation.errors.access_token ? 'error' : null}
                 help={get(validation, 'errors.access_token[0]', 'OpsGenie API Access Token')}
                 value={config.access_token || ''}
                 onChange={this.handleChange}
                 required />

       <Input id="notification-opsGenieFields"
              name="main_fields"
              label="Alert Message Fields (optional)"
              type="textarea"
              bsStyle={validation.errors.main_fields ? 'error' : null}
              help={get(validation, 'errors.main_fields[0]', mainFieldElement)}
              value={config.main_fields || ''}
              onChange={this.handleChange} />
          <Input id="notification-customMessage"
                 name="custom_message"
                 label="Custom Message (optional)"
                 type="textarea"
                 bsStyle={validation.errors.custom_message ? 'error' : null}
                 help={get(validation, 'errors.custom_message[0]', element)}
                 value={config.custom_message || ''}
                 onChange={this.handleChange} />
          <Input id="notification-channel"
              name="channel"
              label="Notify Teams"
              type="text"
              bsStyle={validation.errors.channel ? 'error' : null}
              help={get(validation, 'errors.channel[0]', 'Name of Teams for which the alert will be sent, Add multiple teams as comma separated fields')}
              value={config.channel || ''}
              onChange={this.handleChange}
              required />

          <FormGroup>
            <ControlLabel>Message Backlog Limit (optional)</ControlLabel>
            <InputGroup>
              <InputGroup.Addon>
                <input id="toggle_backlog_size"
                       type="checkbox"
                       checked={isBacklogSizeEnabled}
                       onChange={this.toggleBacklogSize} />
              </InputGroup.Addon>
              <FormControl type="number"
                           id="backlog_size"
                           name="backlog_size"
                           onChange={this.handleBacklogSizeChange}
                           value={backlogSize}
                           min="0"
                           disabled={!isBacklogSizeEnabled} />
            </InputGroup>
            <HelpBlock>Limit the number of backlog messages sent as part of the Microsoft OpsGenie notification.  If set to 0, no limit will be enforced.</HelpBlock>
          </FormGroup>
                <Input id="notification-userName"
                       name="user_name"
                       label="User Name (optional)"
                       type="text"
                       bsStyle={validation.errors.user_name ? 'error' : null}
                       help={get(validation, 'errors.user_name[0]', 'User name of the sender in OpsGenie')}
                       value={config.user_name || ''}
                       onChange={this.handleChange} />
               <Input id="notification-tags"
                      name="tags"
                      label="Tags (optional)"
                      type="text"
                      bsStyle={validation.errors.user_name ? 'error' : null}
                      help={get(validation, 'errors.tags[0]', 'Comma separated list of tags for the Alert message')}
                      value={config.tags || ''}
                      onChange={this.handleChange} />

        </>
      );
    }
}

export default OpsGenieNotificationForm;
