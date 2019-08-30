const Routes = {
  INTEGRATIONS: {
    AWS: {
      SERVICES: '/integrations/aws',
      CLOUDWATCH: {
        index: '/integrations/aws/cloudwatch',
        step: step => `/integrations/aws/cloudwatch/${step}`,
      },
    },
  },
};

const ApiRoutes = {
  INTEGRATIONS: {
    AWS: {
      PERMISSIONS: '/plugins/org.graylog.integrations/aws/permissions',
      REGIONS: '/plugins/org.graylog.integrations/aws/regions',
      CLOUDWATCH: {
        GROUPS: '/plugins/org.graylog.integrations/aws/cloudwatch/log_groups',
      },
      KINESIS: {
        HEALTH_CHECK: '/plugins/org.graylog.integrations/aws/kinesis/health_check',
        STREAMS: '/plugins/org.graylog.integrations/aws/kinesis/streams',
        SAVE: '/plugins/org.graylog.integrations/aws/inputs',
      },
      KINESIS_AUTO_SETUP: {
        CREATE_STREAM: '/plugins/org.graylog.integrations/aws/kinesis/auto_setup/create_stream',
        CREATE_SUBSCRIPTION_POLICY: '/plugins/org.graylog.integrations/aws/kinesis/auto_setup/create_subscription_policy',
        CREATE_SUBSCRIPTION: '/plugins/org.graylog.integrations/aws/kinesis/auto_setup/create_subscription',
      },
    },
  },
};

export default Routes;

export { ApiRoutes };
