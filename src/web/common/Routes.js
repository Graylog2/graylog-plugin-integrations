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

export default Routes;
