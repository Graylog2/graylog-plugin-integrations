import React from 'react';
import { Col, Row } from 'react-bootstrap';
import styled from "styled-components";

const KinesisSetupStep = ({ step }) => {

  return (
    <Row>
      <Col md={8}>
        <p>
          <Row>
          {step.state.type === 'pending' ?
            <i className={`fa fa-hourglass-start fa-2x`} style={{ color: '#919191' }}/> : ""}
          {step.state.type === 'success' ? <i className={`fa fa-check fa-2x`} style={{ color: '#00AE42' }}/> : ""}
          {step.state.type === 'error' ? <i className={`fa fa-times fa-2x`} style={{ color: '#00AE42' }}/> : ""}
          <StepHeader><span>{step.label}</span></StepHeader>
          </Row>
          <Row>
          <span>{'Status: ' + step.state.type}</span><br/>
          <span>{'Details: ' + step.state.additional}</span>
          </Row>
        </p>
      </Col>
    </Row>
  );

};

const StepHeader = styled.span`
  font-size: 18px;
  position: absolute;
  left: 30px;
`;

export default KinesisSetupStep;
