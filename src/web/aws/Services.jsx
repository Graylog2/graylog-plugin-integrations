import React from 'react';

import ROUTES from '../common/Routes';

const Services = () => {
  return (
    <div>
        List of Services.

      <ul><li><a href={ROUTES.INTEGRATIONS.CLOUDWATCH}>CloudWatch</a></li></ul>
    </div>
  );
};

export default Services;
