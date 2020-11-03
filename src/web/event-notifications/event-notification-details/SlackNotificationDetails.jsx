import * as React from 'react';
import PropTypes from 'prop-types';

import {ReadOnlyFormGroup} from 'components/common';
import {Well} from 'components/graylog';
import styles from './SlackNotificationSummary.css';


const SlackNotificationDetails = ({ notification }) => (
  <>
    <ReadOnlyFormGroup label="Webhook URL" value={notification.config.webhook_url} />
    <ReadOnlyFormGroup label="Channel" value={notification.config.channel} />
    <ReadOnlyFormGroup label="Custom Message Template "
                       value={(
                         <Well bsSize="small" className={styles.bodyPreview} >
                           {notification.config.custom_message || <em>Empty body</em>}
                         </Well>
                       )} />
      <ReadOnlyFormGroup label="User Name" value={notification.config.username} />
      <ReadOnlyFormGroup label="Notify Channel" value={notification.config.notify_channel} />
      <ReadOnlyFormGroup label="Link Names" value={notification.config.link_names} />
      <ReadOnlyFormGroup label="Icon URL" value={notification.config.icon_url} />
      <ReadOnlyFormGroup label="Icon Emoji" value={notification.config.icon_emoji} />
  </>
);

SlackNotificationDetails.propTypes = {
  notification: PropTypes.object.isRequired,
};

export default SlackNotificationDetails;
