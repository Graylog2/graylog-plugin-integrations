import React from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import PageHeader from 'components/common/PageHeader';

import { FormDataProvider } from './context/FormData';
import { StepsProvider } from './context/Steps';
import { ApiProvider } from './context/Api';
import CloudWatch from './CloudWatch';

const CloudWatchApp = ({ params: { step }, route, router }) => {
  return (
    <ApiProvider>
      <StepsProvider>
        <FormDataProvider>
          <Row>
            <Col md={12}>
              <PageHeader title="AWS Integration">
                <span>Lorem ipsum dolor sit amet consectetur adipisicing elit. Impedit quidem quam laborum voluptatum similique expedita voluptatem saepe.</span>
              </PageHeader>
            </Col>

            <Col md={12}>
              <CloudWatch wizardStep={step} route={route} router={router} />
            </Col>
          </Row>
        </FormDataProvider>
      </StepsProvider>
    </ApiProvider>
  );
};

CloudWatchApp.propTypes = {
  params: PropTypes.shape({
    step: PropTypes.string,
  }).isRequired,
  route: PropTypes.any.isRequired,
  router: PropTypes.any.isRequired,
};

export default CloudWatchApp;
