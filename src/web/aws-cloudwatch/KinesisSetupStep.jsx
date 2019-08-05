import React from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

const KinesisSetupStep = ({ label, inProgress, success }) => {

  return (
    <Row>
      <Col md={8}>
        <p>{label}</p>
      </Col>
    </Row>
  );
};

export default KinesisSetupStep;
