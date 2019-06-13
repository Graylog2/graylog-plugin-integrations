import React from 'react';
import PropTypes from 'prop-types';
import { Button, Col, Row } from 'react-bootstrap';

const StepReview = ({ values, onSubmit }) => {
  return (
    <Row>
      <Col md={8}>
        <form onSubmit={onSubmit}>
          <h2>AWS CloudWatch Input Review</h2>
          <p>Review All The Things</p>

          <code>
            <pre>{JSON.stringify(values, null, 2)}</pre>
          </code>

          <Button type="submit">Complete CloudWatch Setup</Button>
        </form>
      </Col>
    </Row>
  );
};

StepReview.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  values: PropTypes.object.isRequired,
};

export default StepReview;
