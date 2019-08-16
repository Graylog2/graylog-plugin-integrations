import React, { useState } from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';
import { Panel } from 'react-bootstrap';

import useFetch from './hooks/useFetch';
import { ApiRoutes } from './Routes';

function Policies({ title, note, policy }) {
  const [opened, setOpened] = useState(false);
  const toggleOpen = () => {
    setOpened(!opened);
  };

  return (
    <div>
      <Header onClick={toggleOpen}>
        <HeaderContent>
          <Title>{opened ? 'Hide' : 'Show'} {title}</Title>
          <Note>{note}</Note>
        </HeaderContent>

        <Icon opened={opened}><i className="fa fa-chevron-right fa-2x" /></Icon>
      </Header>

      <Policy opened={opened}>
        {JSON.stringify(policy, null, 2)}
      </Policy>
    </div>
  );
}

Policies.propTypes = {
  title: PropTypes.string.isRequired,
  note: PropTypes.string.isRequired,
  policy: PropTypes.object.isRequired,
};

export default function Permissions() {
  const [permissionsStatus] = useFetch(ApiRoutes.INTEGRATIONS.AWS.PERMISSIONS);

  return (
    <Panel bsStyle="info" header={<span>AWS Policy Permissions</span>}>
      { /* TODO: Update content */ }
      <p>Lorem ipsum dolor sit amet consectetur, adipisicing elit. Ipsa debitis, voluptatum id illo excepturi, magni recusandae accusamus veritatis repellendus nam voluptas nihil ad dolorum dolores cum laboriosam minima cupiditate necessitatibus.</p>

      {!permissionsStatus.loading && permissionsStatus.data && (
      <>
        <Policies title="Recommended Policy"
                  note="To be able to use all available functionality for Kinesis setup."
                  policy={JSON.parse(permissionsStatus.data.setup_policy)} />

        <Policies title="Least Privilege Policy"
                  note="Doesn&apos;t include Kinesis auto-subscribtion controls."
                  policy={JSON.parse(permissionsStatus.data.auto_setup_policy)} />
      </>
      )}
    </Panel>
  );
}

const Header = styled.header`
  display: flex;
  align-items: center;
  cursor: pointer;
`;

const HeaderContent = styled.div`
  flex-grow: 1;
`;

const Icon = styled.span`
  transform: rotate(${props => (props.opened ? '90deg' : '0deg')});
  transition: transform 150ms ease-in-out;
`;

const Policy = styled.pre`
  overflow: hidden;
  max-height: ${props => (props.opened ? '1000px' : '0')};
  opacity: ${props => (props.opened ? '1' : '0')};
  transition: max-height 150ms ease-in-out, opacity 150ms ease-in-out, margin 150ms ease-in-out, padding 150ms ease-in-out;
  margin-bottom: ${props => (props.opened ? '12px' : '0')};
  padding: ${props => (props.opened ? '9.5px' : '0')};
`;

const Title = styled.h4`
  font-weight: bold;
`;

const Note = styled.p`
  font-style: italic;
`;
