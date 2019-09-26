import React, { useState } from 'react';
import styled from 'styled-components';

import { Table } from 'components/graylog';

import AdditionalFields from 'aws/common/AdditionalFields';

const Automatic = () => {
  const [opened, setOpened] = useState(false);

  const handleToggle = () => {
    setOpened(!opened);
  };

  return (
    <StyledAdditionalFields title="What does this include?" visible={opened} onToggle={handleToggle}>

      <StyledTable condensed>
        <thead>
          <tr>
            <td colSpan="2">
              <Title>Automatic authentication will attempt each of the following in the listed order.</Title>
            </td>
          </tr>
        </thead>

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

      <DocumentationNote>For more information, check out the <a href="https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html" target="_blank" rel="noopener noreferrer">AWS Credential Configuration Documentation <i className="fa fa-external-link fa-sm" /></a></DocumentationNote>
    </StyledAdditionalFields>
  );
};

const Title = styled.p`
  font-weight: bold;
  font-size: 1.2em;
  margin-bottom: 12px;
`;

const StyledTable = styled(Table)`
  && {
    max-width: 90%;
    margin: 18px 0 12px;
  }
`;

const StyledAdditionalFields = styled(AdditionalFields)`
  margin: -12px 0 9px;
  padding: 0;
`;

const DocumentationNote = styled.p`
  font-style: italic;
  margin: 0 0 18px;
`;

export default Automatic;
