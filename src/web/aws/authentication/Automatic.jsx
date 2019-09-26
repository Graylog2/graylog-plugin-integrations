import React from 'react';
import styled from 'styled-components';

import { Table } from 'components/graylog';

const Automatic = () => {
  return (
    <div>
      <StyledTable condensed>
        <tbody>
          <tr>
            <th>Environment variables</th>
            <td><code>AWS_ACCESS_KEY_ID</code> and <code>AWS_SECRET_ACCESS_KEY</code></td>
          </tr>
          <tr>
            <th>Java system properties</th>
            <td><code>aws.accessKeyId</code> and <code>aws.secretKey</code></td>
          </tr>
          <tr>
            <th>Default credential profiles file</th>
            <td>Typically located at <code>~/.aws/credentials</code></td>
          </tr>
          <tr>
            <th>Amazon ECS container credentials</th>
            <td>Loaded from the Amazon ECS if the environment variable <code>AWS_CONTAINER_CREDENTIALS_RELATIVE_URI</code> is set</td>
          </tr>
          <tr>
            <th>Instance profile credentials</th>
            <td>Used on EC2 instances, and delivered through the Amazon EC2 metadata service</td>
          </tr>
        </tbody>
      </StyledTable>
    </div>
  );
};

const StyledTable = styled(Table)`
  && {
    max-width: 75%;
    margin: 25px 0;
  }
`;

export default Automatic;
