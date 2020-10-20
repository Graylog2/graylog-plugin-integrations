import React from 'react';
import PropTypes from 'prop-types';
import lodash from 'lodash';

import { Input } from 'components/bootstrap';
import FormsUtils from 'util/FormsUtils';
import { Button, ControlLabel, FormGroup, HelpBlock } from 'components/graylog';
import { ColorPickerPopover } from 'components/common';
import ColorLabel from 'components/sidecars/common/ColorLabel';

class SlackNotificationForm extends React.Component {
    static propTypes = {
      config: PropTypes.object.isRequired,
      validation: PropTypes.object.isRequired,
      onChange: PropTypes.func.isRequired,
    };

    static defaultConfig = {
      color: '#FF0000',
      webhook_url: '',
      channel: '#channel',
      /* eslint-disable no-template-curly-in-string */
      custom_message: ''
                      + '--- [Event Definition] ---------------------------\n'
                      + 'Title:       ${event_definition_title}\n'
                      + 'Type:        ${event_definition_type}\n'
                      + '--- [Event] --------------------------------------\n'
                      + 'Timestamp:            ${event.timestamp}\n'
                      + 'Message:              ${event.message}\n'
                      + 'Source:               ${event.source}\n'
                      + 'Key:                  ${event.key}\n'
                      + 'Priority:             ${event.priority}\n'
                      + 'Alert:                ${event.alert}\n'
                      + 'Timestamp Processing: ${event.timestamp}\n'
                      + 'Timerange Start:      ${event.timerange_start}\n'
                      + 'Timerange End:        ${event.timerange_end}\n'
                      + 'Event Fields:\n'
                      + '${foreach event.fields field}\n'
                      + '${field.key}: ${field.value}\n'
                      + '${end}\n'
                      + '${if backlog}\n'
                      + '--- [Backlog] ------------------------------------\n'
                      + 'Last messages accounting for this alert:\n'
                      + '${foreach backlog message}\n'
                      + '${message.timestamp}  ::  ${message.source}  ::  ${message.message}\n'
                      + '${message.message}\n'
                      + '${end}'
                      + '${end}\n',
      /* eslint-enable no-template-curly-in-string */
      user_name: 'Graylog',
      notify_channel: false,
      link_names: false,
      icon_url: '',
      icon_emoji: '',

    };

    propagateChange = (key, value) => {
      const { config, onChange } = this.props;
      const nextConfig = lodash.cloneDeep(config);
      nextConfig[key] = value;
      onChange(nextConfig);
    };

    handleColorChange = (color, _, hidePopover) => {
      hidePopover();
      this.propagateChange('color', color);
    };

    handleChange = (event) => {
      const { name } = event.target;
      this.propagateChange(name, FormsUtils.getValueFromInput(event.target));
    };

    render() {
      const { config, validation } = this.props;

      return (
        <>

          <FormGroup controlId="color">
            <ControlLabel>Configuration color</ControlLabel>
            <div>
              <ColorLabel color={config.color} />
              <div style={{ display: 'inline-block', marginLeft: 15 }}>
                <ColorPickerPopover id="color"
                                    name="color"
                                    color={config.color || '#f06292'}
                                    placement="right"
                                    triggerNode={<Button bsSize="xsmall">Change color</Button>}
                                    onChange={this.handleColorChange} />
              </div>
            </div>
            <HelpBlock>Choose a color to use for this configuration.</HelpBlock>
          </FormGroup>
          <Input id="notification-webhookUrl"
                 name="webhook_url"
                 label="Webhook URL"
                 type="text"
                 bsStyle={validation.errors.webhook_url ? 'error' : null}
                 help={lodash.get(validation, 'errors.webhook_url[0]', 'Slack "Incoming Webhook" URL')}
                 value={config.webhook_url || ''}
                 onChange={this.handleChange}
                 required />
          <Input id="notification-channel"
                 name="channel"
                 label="Channel"
                 type="text"
                 bsStyle={validation.errors.channel ? 'error' : null}
                 help={lodash.get(validation, 'errors.channel[0]', 'Name of Slack #channel or @user for a direct message')}
                 value={config.channel || ''}
                 onChange={this.handleChange}
                 required />
          <Input id="notification-customMessage"
                 name="custom_message"
                 label="Custom Message (optional)"
                 type="textarea"
                 bsStyle={validation.errors.custom_message ? 'error' : null}
                 help={lodash.get(validation, 'errors.custom_message[0]', 'Custom message to be appended below the alert title. The following properties are available for template building: "event_definition", "event", "backlog", "backlog_size", "streams", "graylog_url". See http://docs.graylog.org/en/3.1/pages/streams/alerts.html for more details.')}
                 value={config.custom_message || ''}
                 onChange={this.handleChange} />
          <Input id="notification-userName"
                 name="user_name"
                 label="User Name (optional)"
                 type="text"
                 bsStyle={validation.errors.user_name ? 'error' : null}
                 help={lodash.get(validation, 'errors.user_name[0]', 'User name of the sender in Slack')}
                 value={config.user_name || ''}
                 onChange={this.handleChange} />
          <Input id="notification-notifyChannel"
                 name="notify_channel"
                 label="Notify Channel (optional)"
                 type="checkbox"
                 bsStyle={validation.errors.notify_channel ? 'error' : null}
                 help={lodash.get(validation, 'errors.notify_channel[0]', 'Notify all users in channel by adding @channel to the message')}
                 checked={config.notify_channel || ''}
                 onChange={this.handleChange} />
          <Input id="notification-linkNames"
                 name="link_names"
                 label="Link Names (optional)"
                 type="checkbox"
                 bsStyle={validation.errors.link_names ? 'error' : null}
                 help={lodash.get(validation, 'errors.link_names[0]', 'Find and link channel names and user names')}
                 checked={config.link_names || ''}
                 onChange={this.handleChange} />
          <Input id="notification-iconUrl"
                 name="icon_url"
                 label="Icon URL (optional)"
                 type="text"
                 bsStyle={validation.errors.icon_url ? 'error' : null}
                 help={lodash.get(validation, 'errors.icon_url[0]', 'Image to use as the icon for this message')}
                 value={config.icon_url || ''}
                 onChange={this.handleChange} />
          <Input id="notification-iconEmoji"
                 name="icon_emoji"
                 label="Icon Emoji (optional)"
                 type="text"
                 bsStyle={validation.errors.icon_emoji ? 'error' : null}
                 help={lodash.get(validation, 'errors.icon_emoji[0]', 'Emoji to use as the icon for this message (overrides Icon URL)')}
                 value={config.icon_emoji || ''}
                 onChange={this.handleChange} />
        </>
      );
    }
}

export default SlackNotificationForm;
