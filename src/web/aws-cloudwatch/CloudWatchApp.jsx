import React from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import PageHeader from 'components/common/PageHeader';

import { FormDataProvider } from './providers/FormData';
import { StepsProvider } from './reducers/StepsContext';
import CloudWatch from './CloudWatch';

function CloudWatchApp({ params: { step } }) {
  return (
    <StepsProvider>
      <FormDataProvider>
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
      </FormDataProvider>
    </StepsProvider>
  );
}

CloudWatchApp.propTypes = {
  params: PropTypes.shape({
    step: PropTypes.string,
  }).isRequired,
};

export default CloudWatchApp;
