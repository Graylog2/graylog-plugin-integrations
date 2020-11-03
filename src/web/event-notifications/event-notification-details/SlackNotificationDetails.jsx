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
  </>
);

SlackNotificationDetails.propTypes = {
  notification: PropTypes.object.isRequired,
};

export default SlackNotificationDetails;
