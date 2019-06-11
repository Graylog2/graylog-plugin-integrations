import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Row } from 'react-bootstrap';

// import UserNotification from 'util/UserNotification';
import Wizard from 'components/common/Wizard';

import CloudWatchAuthorize from './StepAuthorize'
import CloudWatchKinesis from './StepKinesis'
import CloudWatchHealthCheck from './StepHealthCheck'
import CloudWatchReview from './StepReview'

export default class AWSCloudWatch extends Component {
  constructor(props) {
    super(props);

    this.wizardSteps = [
      {
        key: 'authorize',
        title: 'AWS CloudWatch Authorize',
        component: (<CloudWatchAuthorize onSubmit={this.handleSubmit}
                                         onChange={this.handleFieldUpdate}
                                         getValue={this.getFormData} />),
      },
      {
        key: 'kinesis-setup',
        title: 'AWS CloudWatch Kinesis Setup',
        component: (<CloudWatchKinesis onSubmit={this.handleSubmit}
                                       onChange={this.handleFieldUpdate}
                                       getValue={this.getFormData}
                                       hasStreams />),
      },
      {
        key: 'health-check',
        title: 'AWS CloudWatch Health Check',
        component: (<CloudWatchHealthCheck onSubmit={this.handleSubmit} />),
      },
      {
        key: 'review',
        title: 'AWS CloudWatch Review',
        component: (<CloudWatchReview onSubmit={this.handleSubmit} getAllValues={this.getAllFormData} />),
      },
    ];

    this.state = {
      currentStep: 'authorize',
      enabledSteps: ['authorize'],
      formData: {},
      wizardSteps: this.wizardWithDisabledSteps(),
    };

    this.availableSteps = this.wizardSteps.map(step => step.key);
  }

  static propTypes = {
    params: PropTypes.shape({
      step: PropTypes.string,
    }),
  }

  wizardWithDisabledSteps = () => {
    return this.wizardSteps.map((step) => (
       {
        ...step,
        disabled: this.isDisabledStep(step.key),
      }
    ));
  }

  getFormData = (value) => {
    return this.state.formData[value];
  }

  getAllFormData = () => this.state.formData;

  isDisabledStep = (step) => {
    if (!this.state) {
      return true;
    }

    const { enabledSteps } = this.state;

    if (!enabledSteps || enabledSteps.length === 0) {
      return true;
    };

    return !enabledSteps.includes(step)
  };

  handleFieldUpdate = ({ target }) => {
    const { formData } = this.state;

    this.setState({
      formData: {
        ...formData,
        [target.id]: target.value
      }
    })
  }

  handleSubmit = (formData) => {
    formData.preventDefault();

    const { currentStep, enabledSteps } = this.state;
    const nextStep = this.availableSteps.indexOf(currentStep) + 1;

    if (this.wizardSteps[nextStep]) {
      const key = this.wizardSteps[nextStep].key;

      this.setState({
        enabledSteps: [...enabledSteps, key],
        currentStep: key,
      }, () => {
        this.setState({
          wizardSteps: this.wizardWithDisabledSteps(this.wizardSteps),
        });
      });
    }

    // UserNotification.success(`Form Data Was Submitted`, 'Hurray!');
  }

  handleStepChange = (currentStep) => {
    this.setState({
      currentStep,
    })
  }

  render() {
    const { currentStep, wizardSteps } = this.state;

    return (
      <Row>
        <Wizard
          steps={wizardSteps}
          activeStep={currentStep}
          onStepChange={this.handleStepChange}
          horizontal
          justified
          hidePreviousNextButtons
        />
      </Row>
    )
  }
}
