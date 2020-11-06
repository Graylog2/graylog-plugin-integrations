import React from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';

import { Icon } from 'components/common';

const KinesisSetupStep = ({ label, progress }) => {
  const { data, error, loading } = progress;

  const waitingText = !data && !loading && !error && 'Waiting...';
  const loadingText = loading ? `Creating ${label}` : waitingText;
  const successText = data ? data.result : loadingText;
  const defaultText = error || successText;

  return (
    <StepItem>
      <IconWrap>
        {!data && !loading && !error && <Icon name="hourglass-start" size="2x" style={{ color: '#DCE1E5' }} />}
        {loading && <Icon name="spinner" size="2x" style={{ color: '#0063BE' }} />}
        {data && <Icon name="check" size="2x" style={{ color: '#00AE42' }} />}
        {error && <Icon name="times" size="2x" style={{ color: '#AD0707' }} />}
      </IconWrap>

      <Content>
        <StepHeader>Create {label}</StepHeader>

        <StepDetails>
          {defaultText}
        </StepDetails>
      </Content>
    </StepItem>
  );
};

KinesisSetupStep.propTypes = {
  progress: PropTypes.shape({
    data: PropTypes.object,
    error: PropTypes.object,
    loading: PropTypes.bool,
  }).isRequired,
  label: PropTypes.string.isRequired,
};

const StepItem = styled.li`
  display: flex;
  margin: 0 0 12px;
`;

const IconWrap = styled.div`
  min-width: 36px;
`;

const Content = styled.div`
  flex-grow: 1;
`;

const StepHeader = styled.span`
  font-size: 18px;
`;

const StepDetails = styled.p`
  margin: 3px 0 0;
`;

export default KinesisSetupStep;
