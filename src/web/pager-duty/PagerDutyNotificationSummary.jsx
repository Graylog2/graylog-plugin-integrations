import React from 'react';
import PropTypes from 'prop-types';
import CommonNotificationSummary from 'components/event-notifications/event-notification-types/CommonNotificationSummary';

function PagerDutyNotificationSummary({ notification, ...restProps }) {
    return (
        <CommonNotificationSummary {...restProps}>
            <>
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
            </>
        </CommonNotificationSummary>
    );
}

PagerDutyNotificationSummary.propTypes = {
    type: PropTypes.string.isRequired,
    notification: PropTypes.shape,
    definitionNotification: PropTypes.shape.isRequired,
};

PagerDutyNotificationSummary.defaultProps = {
    notification: {},
};

export default PagerDutyNotificationSummary;
