import React from 'react';
import { Col, Row } from 'react-bootstrap';
import styled from "styled-components";

const KinesisSetupStep = ({ step }) => {

  return (
    <Col md={8}>
      <Row>
        {step.state.type === 'pending' ?
          <i className={`fa fa-hourglass-start fa-2x`} style={{ color: '#919191' }}/> : ""}
        {step.state.type === 'success' ? <i className={`fa fa-check fa-2x`} style={{ color: '#00AE42' }}/> : ""}
        {step.state.type === 'error' ? <i className={`fa fa-times fa-2x`} style={{ color: '#D43F3F' }}/> : ""}
        <StepHeader><span>{step.label}</span></StepHeader>
      </Row>
      <Row>
        <p>
          <StepDetails>
            <span>{step.state.additional}</span>
          </StepDetails>
        </p>
      </Row>
    </Col>
  );
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
