import React from 'react';
import PropTypes from 'prop-types';
import { Button, Col, Row } from 'react-bootstrap';

const StepReview = ({ getAllValues, onSubmit }) => {
  return (
    <Row>
      <Col md={8}>
        <form onSubmit={onSubmit}>
          <h2>AWS CloudWatch Input Review</h2>
          <p>Review All The Things</p>

          <code>
            <pre>{JSON.stringify(getAllValues(), null, 2)}</pre>
          </code>

          <Button type="submit">Complete CloudWatch Setup</Button>
        </form>
      </Col>
    </Row>
  );
};

StepReview.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  getAllValues: PropTypes.func.isRequired,
};

export default StepReview;
