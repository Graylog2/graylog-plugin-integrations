import React from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';

const KinesisSetupStep = ({ step }) => {
  return (
    <>
      {step.state.type === 'pending'
        ? <i className="fa fa-hourglass-start fa-2x" style={{ color: '#919191' }} /> : ''}
      {step.state.type === 'success' ? <i className="fa fa-check fa-2x" style={{ color: '#00AE42' }} /> : ''}
      {step.state.type === 'error' ? <i className="fa fa-times fa-2x" style={{ color: '#D43F3F' }} /> : ''}
      <StepHeader><span>{step.label}</span></StepHeader>

      <p>
        <StepDetails>
          <span>{step.state.additional}</span>
        </StepDetails>
      </p>
    </>
  );
};

KinesisSetupStep.propTypes = {
  step: PropTypes.shape({
    state: PropTypes.object,
    label: PropTypes.string,
  }).isRequired,
};

const StepHeader = styled.span`
  font-size: 18px;
  position: absolute;
  left: 30px;
`;

const StepDetails = styled.span`
  margin-left: 10px;
`;

export default KinesisSetupStep;
