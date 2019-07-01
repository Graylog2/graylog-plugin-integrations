import 'webpack-entry';

import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';

import AWSServices from './aws/Services';
import AWSCloudWatch from './aws-cloudwatch/CloudWatch';

import ROUTES from '../common/Routes';
import packageJson from '../../package.json';

const manifest = new PluginManifest(packageJson, {
  routes: [
    { path: ROUTES.INTEGRATIONS.SERVICES, component: AWSServices },
    { path: ROUTES.INTEGRATIONS.CLOUDWATCH, component: AWSCloudWatch },
    { path: ROUTES.INTEGRATIONS.CLOUDWATCH_STEP(':step'), component: AWSCloudWatch },
  ],
});

PluginStore.register(manifest);
