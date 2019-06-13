import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Col, Row } from 'react-bootstrap';

import Wizard from 'components/common/Wizard';

import StepAuthorize from './StepAuthorize';
import StepKinesis from './StepKinesis';
import StepHealthCheck from './StepHealthCheck';
import StepReview from './StepReview';

export default class AWSCloudWatch extends Component {
  static propTypes = {
    params: PropTypes.shape({
      step: PropTypes.string,
    }).isRequired,
  }

  constructor(props) {
    super(props);

    this.wizardSteps = [
      {
        key: 'authorize',
        title: 'AWS CloudWatch Authorize',
        component: (<StepAuthorize onSubmit={this.handleSubmit}
                                   onChange={this.handleFieldUpdate}
                                   getValue={this.getFormData} />),
      },
      {
        key: 'kinesis-setup',
        title: 'AWS CloudWatch Kinesis Setup',
        component: (<StepKinesis onSubmit={this.handleSubmit}
                                 onChange={this.handleFieldUpdate}
                                 getValue={this.getFormData}
                                 advOptions={this._advancedOptions}
                                 hasStreams />),
      },
      {
        key: 'health-check',
        title: 'AWS CloudWatch Health Check',
        component: (<StepHealthCheck onSubmit={this.handleSubmit} />),
      },
      {
        key: 'review',
        title: 'AWS CloudWatch Review',
        component: (<StepReview onSubmit={this.handleSubmit} getAllValues={this.getAllFormData} />),
      },
    ];

    this.state = {
      advOptionsOpened: false,
      currentStep: 'kinesis-setup',
      enabledSteps: ['authorize', 'kinesis-setup'],
      formData: {
        // Default Advanced Values
        awsCloudWatchGlobalInput: '',
        awsCloudWatchAssumeARN: '',
        awsCloudWatchBatchSize: '10000',
        awsCloudWatchThrottleEnabled: '',
        awsCloudWatchThrottleWait: '1000',
        // End Default Values
        awsCloudWatchName: 'test',
        awsCloudWatchDescription: 'test',
        awsCloudWatchAwsKey: '123',
        awsCloudWatchAwsSecret: '123',
        awsCloudWatchAwsRegion: 'us-east-2',
        awsCloudWatchKinesisStream: 'stream-name-2',
      },
      wizardSteps: this._wizardWithDisabledSteps(),
    };

    this.availableSteps = this.wizardSteps.map(step => step.key);
  }

  _wizardWithDisabledSteps = () => {
    return this.wizardSteps.map(step => (
      {
        ...step,
        disabled: this.isDisabledStep(step.key),
      }
    ));
  }

  _advancedOptions = () => {
    const { advOptionsOpened } = this.state;

    console.log('_advancedOptions', advOptionsOpened);

    return {
      toggle: this.toggleAdvancedOptions,
      opened: advOptionsOpened,
    };
  }

  /* eslint-disable-next-line react/destructuring-assignment */
  getFormData = value => this.state.formData[value];

  /* eslint-disable-next-line react/destructuring-assignment */
  getAllFormData = () => this.state.formData;

  isDisabledStep = (step) => {
    if (!this.state) {
      return true;
    }

    const { enabledSteps } = this.state;

    if (!enabledSteps || enabledSteps.length === 0) {
      return true;
    }

    return !enabledSteps.includes(step);
  };

  handleFieldUpdate = ({ target }) => {
    const { formData } = this.state;
    const isChecked = Object.keys(target).includes('checked');
    let { value } = target;

    if (isChecked) {
      value = target.checked ? value : '';
    }

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

    if (this.wizardSteps[nextStep]) {
      const { key } = this.wizardSteps[nextStep];

      this.setState({
        enabledSteps: [...enabledSteps, key],
        currentStep: key,
      }, () => {
        this.setState({
          wizardSteps: this._wizardWithDisabledSteps(),
        });
      });
    }

    // UserNotification.success(`Form Data Was Submitted`, 'Hurray!');
  }

  handleStepChange = (currentStep) => {
    this.setState({
      currentStep,
    });
  }

  toggleAdvancedOptions = () => {
    const { advOptionsOpened } = this.state;

    console.log('toggleAdvancedOptions', !advOptionsOpened);

    this.setState({
      advOptionsOpened: !advOptionsOpened,
    });
  }

  render() {
    const { currentStep, wizardSteps } = this.state;

    console.log('render CloudWatch');

    return (
      <Row>
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
