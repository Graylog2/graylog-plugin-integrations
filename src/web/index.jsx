import 'webpack-entry';

import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';
import AWSServices from './aws/Services'
import AWSCloudWatch from './aws-cloudwatch/CloudWatch'
import packageJson from '../../package.json';

const manifest = new PluginManifest(packageJson, {
  routes: [
   { path: '/aws', component: AWSServices },
   { path: '/aws/cloudwatch', component: AWSCloudWatch },
   { path: '/aws/cloudwatch/:step', component: AWSCloudWatch },
  ],
});

PluginStore.register(manifest);
