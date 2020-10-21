import 'webpack-entry';

import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';

import Routes from 'aws/common/Routes';

import AWSInputConfiguration from './aws/AWSInputConfiguration';
import AWSCloudWatchApp from './aws/cloudwatch/CloudWatchApp';

import packageJson from '../../package.json';
import PagerDutyNotificationForm from "./pager-duty/PagerDutyNotificationForm";
import PagerDutyNotificationSummary from "./pager-duty/PagerDutyNotificationSummary";

const manifest = new PluginManifest(packageJson, {
  routes: [
    { path: Routes.INTEGRATIONS.AWS.CLOUDWATCH.index, component: AWSCloudWatchApp },
  ],
  inputConfiguration: [
    {
      type: 'org.graylog.integrations.aws.inputs.AWSInput',
      component: AWSInputConfiguration,
    },
  ],
  eventNotificationTypes: [
    {
      type: 'pagerduty-notification-v1',
      displayName: 'Pager Duty Notification',
      formComponent: PagerDutyNotificationForm,
      summaryComponent: PagerDutyNotificationSummary,
      defaultConfig: PagerDutyNotificationForm.defaultConfig,
    },
  ],
});

PluginStore.register(manifest);
