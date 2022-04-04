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
import CommonNotificationSummary from 'components/event-notifications/event-notification-types/CommonNotificationSummary';
import { OpsGenieNotificationSummaryType } from '../types';

function OpsGenieNotificationSummary({ notification, ...restProps }:React.FC<OpsGenieNotificationSummaryType>) {
  return (
    <CommonNotificationSummary {...restProps} notification={notification}>
      <tr>
        <td>Color</td>
        <td>{notification?.config?.color}</td>
      </tr>
      <tr>
        <td>Webhook URL</td>
        <td>{notification.config.access_token}</td>
      </tr>

      <tr>
        <td>Custom Message</td>
        <td>{notification.config.custom_message}</td>
      </tr>
      <tr>
         <td>Custom Message</td>
         <td>{notification.config.main_fields}</td>
       </tr>
      <tr>
        <td>Message Backlog Limit</td>
        <td>{notification.config.backlog_size}</td>
      </tr>
        <tr>
          <td>User Name</td>
          <td>{notification.config.user_name}</td>
        </tr>
      <tr>
        <td>Graylog URL</td>
        <td>{notification.config.graylog_url}</td>
      </tr>
    </CommonNotificationSummary>
  );
}

OpsGenieNotificationSummary.defaultProps = {
  notification: {},
};

export default OpsGenieNotificationSummary;
