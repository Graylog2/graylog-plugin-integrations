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
      REGIONS: '/plugins/org.graylog.integrations/aws/regions',
      CLOUDWATCH: {
        GROUPS: '/plugins/org.graylog.integrations/aws/cloudwatch/log_groups',
      },
      KINESIS: {
        HEALTH_CHECK: '/plugins/org.graylog.integrations/aws/kinesis/health_check',
        STREAMS: '/plugins/org.graylog.integrations/aws/kinesis/streams',
        SAVE: '/plugins/org.graylog.integrations/aws/inputs',
      },
    },
  },
};

export default Routes;

export { ApiRoutes };
