import 'webpack-entry';

import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';

import Routes from 'common/Routes.js';

import AWSServices from './aws/Services';
import AWSCloudWatch from './aws-cloudwatch/CloudWatch';

import packageJson from '../../package.json';

const manifest = new PluginManifest(packageJson, {
  routes: [
    { path: Routes.INTEGRATIONS.AWS.SERVICES, component: AWSServices },
    { path: Routes.INTEGRATIONS.AWS.CLOUDWATCH.index, component: AWSCloudWatch },
    { path: Routes.INTEGRATIONS.AWS.CLOUDWATCH.step(':step'), component: AWSCloudWatch },
  ],
});

PluginStore.register(manifest);
