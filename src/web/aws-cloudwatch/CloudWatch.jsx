import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import Wizard from 'components/common/Wizard';
import PageHeader from 'components/common/PageHeader';

import StepAuthorize from './StepAuthorize';
import StepKinesis from './StepKinesis';
import StepHealthCheck from './StepHealthCheck';
import StepReview from './StepReview';
import DEFAULT_VALUES from './default_values';

export default class AWSCloudWatch extends Component {
  static propTypes = {
    params: PropTypes.shape({
      step: PropTypes.string,
    }).isRequired,
  }

  logOutput = { // Demo Data until API is wired
    full_message: '2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 ACCEPT OK',
    version: 2,
    'account-id': 123456789010,
    'interface-id': 'eni-abc123de',
    src_addr: '172.31.16.139',
    dst_addr: '172.31.16.21',
    src_port: 20641,
    dst_port: 22,
    protocol: 6,
    packets: 20,
    bytes: 4249,
    start: 1418530010,
    end: 1418530070,
    action: 'ACCEPT',
    'log-status': 'OK',
  }

  constructor(props) {
    super(props);

    this.state = {
      visibleAdvancedOptions: false,
      currentStep: 'authorize',
      enabledSteps: ['authorize'],
      formData: {
        ...DEFAULT_VALUES,
      },
      logOutput: JSON.stringify(this.logOutput, null, 2),
    };
  }

  isDisabledStep = (step) => {
    const { enabledSteps } = this.state;

    if (!enabledSteps || enabledSteps.length === 0) {
      return true;
    }

    return !enabledSteps.includes(step);
  };

  handleFieldUpdate = ({ target }) => {
    const { formData } = this.state;
    const value = Object.keys(target).includes('checked') ? target.checked : target.value;

    this.setState({
      formData: {
        ...formData,
        [target.name || target.id]: value,
      },
    });
  }

  handleSubmit = (formData) => {
    formData.preventDefault();

    const { currentStep, enabledSteps } = this.state;
    const nextStep = this.availableSteps.indexOf(currentStep) + 1;

    if (this.availableSteps[nextStep]) {
      const key = this.availableSteps[nextStep];

      this.setState({
        enabledSteps: [...enabledSteps, key],
        currentStep: key,
      });
    }

    // UserNotification.success(`Form Data Was Submitted`, 'Hurray!');
  }

  handleStepChange = (currentStep) => {
    this.setState({
      currentStep,
    });
  }

  handleEditClick = nextStep => () => {
    this.handleStepChange(nextStep);
  }

  toggleAdvancedOptions = () => {
    const { visibleAdvancedOptions } = this.state;

    this.setState({
      visibleAdvancedOptions: !visibleAdvancedOptions,
    });
  }

  render() {
    const { currentStep, formData, logOutput, visibleAdvancedOptions } = this.state;
    const wizardSteps = [
      {
        key: 'authorize',
        title: 'AWS CloudWatch Authorize',
        component: (<StepAuthorize onSubmit={this.handleSubmit}
                                   onChange={this.handleFieldUpdate}
                                   values={formData} />),
        disabled: this.isDisabledStep('authorize'),
      },
      {
        key: 'kinesis-setup',
        title: 'AWS CloudWatch Kinesis Setup',
        component: (<StepKinesis onSubmit={this.handleSubmit}
                                 onChange={this.handleFieldUpdate}
                                 values={formData}
                                 toggleAdvancedOptions={this.toggleAdvancedOptions}
                                 visibleAdvancedOptions={visibleAdvancedOptions}
                                 hasStreams />),
        disabled: this.isDisabledStep('kinesis-setup'),
      },
      {
        key: 'health-check',
        title: 'AWS CloudWatch Health Check',
        component: (<StepHealthCheck onSubmit={this.handleSubmit} logOutput={logOutput} />),
        disabled: this.isDisabledStep('health-check'),
      },
      {
        key: 'review',
        title: 'AWS CloudWatch Review',
        component: (<StepReview onSubmit={this.handleSubmit}
                                values={formData}
                                logOutput={logOutput}
                                onEditClick={this.handleEditClick} />),
        disabled: this.isDisabledStep('review'),
      },
    ];
    this.availableSteps = wizardSteps.map(step => step.key);

    return (
      <Row>
        <Col md={12}>
          <PageHeader title="AWS Integration">
            <span>Lorem ipsum dolor sit amet consectetur adipisicing elit. Impedit quidem quam laborum voluptatum similique expedita voluptatem saepe.</span>
          </PageHeader>
        </Col>
        <Col md={12}>
          <Wizard steps={wizardSteps}
                  activeStep={currentStep}
                  onStepChange={this.handleStepChange}
                  horizontal
                  justified
                  hidePreviousNextButtons />
        </Col>
      </Row>
    );
  }
}
