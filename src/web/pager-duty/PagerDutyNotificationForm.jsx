import React from 'react';
import PropTypes from 'prop-types';
import get from 'lodash/get';
import cloneDeep from 'lodash/cloneDeep';

import { Input } from 'components/bootstrap';
import FormsUtils from 'util/FormsUtils';

class PagerDutyNotificationForm extends React.Component {
    static propTypes = {
        config: PropTypes.shape.isRequired,
        validation: PropTypes.shape.isRequired,
        onChange: PropTypes.func.isRequired,
    };

    static defaultConfig = {
        client_name: 'Graylog',
        client_url: '',
        custom_incident: true,
        key_prefix: 'Graylog/',
        routing_key: '',
    };

    propagateChange = (key, value) => {
        const { config, onChange } = this.props;
        const nextConfig = cloneDeep(config);
        nextConfig[key] = value;
        onChange(nextConfig);
    };

    handleChange = (event) => {
        const { name } = event.target;
        this.propagateChange(name, FormsUtils.getValueFromInput(event.target));
    };

    render() {
        const { config, validation } = this.props;

        return (
            <React.Fragment>
                <Input id="pagerduty-notification-v1-routing_key"
                       name="routing_key"
                       label="Routing Key"
                       type="text"
                       bsStyle={validation.errors.routing_key ? 'error' : null}
                       help={get(validation, 'errors.routing_key[0]', 'The Pager Duty integration Routing Key.')}
                       value={config.routing_key}
                       onChange={this.handleChange}
                       required />
                <Input id="pagerduty-notification-v1-custom_incident"
                       name="custom_incident"
                       label="Use Custom Incident Key"
                       type="checkbox"
                       bsStyle={validation.errors.custom_incident ? 'error' : null}
                       help={get(validation, 'errors.custom_incident[0]', 'Generate a custom incident key based on the Stream and the Alert Condition.')}
                       value={config.custom_incident}
                       checked={config.custom_incident}
                       onChange={this.handleChange} />
                <Input id="pagerduty-notification-v1-key_prefix"
                       name="key_prefix"
                       label="Incident Key Prefix"
                       type="text"
                       bsStyle={validation.errors.key_prefix ? 'error' : null}
                       help={get(validation, 'errors.key_prefix[0]', 'Incident key prefix that identifies the incident.')}
                       value={config.key_prefix}
                       onChange={this.handleChange}
                       required />
                <Input id="pagerduty-notification-v1-client_name"
                       name="client_name"
                       label="Client Name"
                       type="text"
                       bsStyle={validation.errors.client_name ? 'error' : null}
                       help={get(validation, 'errors.client_name[0]', 'The name of the Graylog system that is triggering the PagerDuty event.')}
                       value={config.client_name}
                       onChange={this.handleChange}
                       required />
                <Input id="pagerduty-notification-v1-client_url"
                       name="client_url"
                       label="Client URL"
                       type="text"
                       bsStyle={validation.errors.client_url ? 'error' : null}
                       help={get(validation, 'errors.client_url[0]', 'The name of the Graylog system that is triggering the PagerDuty event.')}
                       value={config.client_url}
                       onChange={this.handleChange}
                       required />
            </React.Fragment>
        );
    }
}

export default PagerDutyNotificationForm;