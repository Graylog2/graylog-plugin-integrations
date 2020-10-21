import React from 'react';
import PropTypes from 'prop-types';

import CommonNotificationSummary from 'components/event-notifications/event-notification-types/CommonNotificationSummary';

class PagerDutyNotificationSummary extends React.Component {
    static propTypes = {
        type: PropTypes.string.isRequired,
        notification: PropTypes.shape,
        definitionNotification: PropTypes.shape.isRequired,
    };

    static defaultProps = {
        notification: {},
    };

    render() {
        const { notification } = this.props;

        return (
            <CommonNotificationSummary {...this.props}>
                <React.Fragment>
                    <tr>
                        <td>Routing Key</td>
                        <td><code>{notification?.config?.routing_key}</code></td>
                    </tr>
                    <tr>
                        <td>Use Custom Incident Key</td>
                        <td><code>{notification?.config?.custom_incident ? 'Yes' : 'No'}</code></td>
                    </tr>
                    <tr>
                        <td>Incident Key Prefix</td>
                        <td><code>{notification?.config?.key_prefix}</code></td>
                    </tr>
                    <tr>
                        <td>Client Name</td>
                        <td><code>{notification?.config?.client_name}</code></td>
                    </tr>
                    <tr>
                        <td>Client URL</td>
                        <td><code>{notification?.config?.client_url}</code></td>
                    </tr>
                </React.Fragment>
            </CommonNotificationSummary>
        );
    }
}

export default PagerDutyNotificationSummary;