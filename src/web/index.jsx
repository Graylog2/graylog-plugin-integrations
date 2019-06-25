import 'webpack-entry';

import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';

<<<<<<< HEAD
=======
import ROUTES from 'common/Routes';
>>>>>>> Route constants
import AWSServices from './aws/Services';
import AWSCloudWatch from './aws-cloudwatch/CloudWatch';

import packageJson from '../../package.json';

const manifest = new PluginManifest(packageJson, {
  routes: [
    { path: ROUTES.INTEGRATIONS.AWS.SERVICES, component: AWSServices },
    { path: ROUTES.INTEGRATIONS.AWS.CLOUDWATCH, component: AWSCloudWatch },
  ],
});

PluginStore.register(manifest);
