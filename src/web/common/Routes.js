const ROUTES = {
  INTEGRATIONS: {
    AWS: {
      SERVICES: () => '/integrations/aws',
      CLOUDWATCH: () => '/integrations/aws/cloudwatch',
      CLOUDWATCH_STEP: step => `/integrations/aws/cloudwatch/${step}`,
    },
  },
};

export default ROUTES;
