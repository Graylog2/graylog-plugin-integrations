import React, {useEffect} from 'react';
import { Col, Row } from 'react-bootstrap';

const KinesisSetupStep = ({ step }) => {

  return (
    <Row>
      <Col md={8}>
        <h4>{step.label}</h4>
        <p>
          <span>{'Status: ' + step.state.type}</span><br/>
          <span>{'Details: ' + step.state.additional}</span>
        </p>
      </Col>
    </Row>
  );
};

export default KinesisSetupStep;
