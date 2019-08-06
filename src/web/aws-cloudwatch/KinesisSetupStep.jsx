import React, {useEffect} from 'react';
import { Col, Row } from 'react-bootstrap';

const KinesisSetupStep = ({ label, state }) => {

  return (
    <Row>
      <Col md={8}>
        <h4>{label}</h4>
        <p>
          <span>{'Status: ' + state.type}</span><br/>
          <span>{'Details: ' + state.additional}</span>
        </p>
      </Col>
    </Row>
  );
};

export default KinesisSetupStep;
