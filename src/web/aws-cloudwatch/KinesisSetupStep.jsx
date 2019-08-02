import React from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

const KinesisSetupStep = ({ onSubmit, onEditClick }) => {
  return (
    <Row>
      <Col md={8}>
        <p>Step 1</p>
      </Col>
    </Row>
  );
};

KinesisSetupStep.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onEditClick: PropTypes.func.isRequired,
};

export default KinesisSetupStep;
