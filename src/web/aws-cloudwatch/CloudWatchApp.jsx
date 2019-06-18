import React from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import PageHeader from 'components/common/PageHeader';

import { CloudWatchProvider } from './CloudWatchContext';
import CloudWatch from './CloudWatch';

function CloudWatchApp({ params: { step } }) {
  return (
    <CloudWatchProvider>
      <Row>
        <Col md={12}>
          <PageHeader title="AWS Integration">
            <span>Lorem ipsum dolor sit amet consectetur adipisicing elit. Impedit quidem quam laborum voluptatum similique expedita voluptatem saepe.</span>
          </PageHeader>
        </Col>
        <Col md={12}>
          <CloudWatch wizardStep={step} />
        </Col>
      </Row>
    </CloudWatchProvider>
  );
}

CloudWatchApp.propTypes = {
  params: PropTypes.shape({
    step: PropTypes.string,
  }).isRequired,
};

export default CloudWatchApp;
