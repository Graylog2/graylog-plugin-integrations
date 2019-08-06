import React from 'react';
import { Col, Row } from 'react-bootstrap';

const KinesisSetupStep = ({ label, inProgress, success }) => {

  return (
    <Row>
      <Col md={8}>
        <h4>{label}</h4>
        <p>
          <span>{'In progress: ' + inProgress}</span><br/>
          <span>{'Success: ' + success}</span>
        </p>
      </Col>
    </Row>
  );
};

export default KinesisSetupStep;
