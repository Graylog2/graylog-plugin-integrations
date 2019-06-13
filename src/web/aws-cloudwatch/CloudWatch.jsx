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

    this.state = {
      advOptionsOpened: false,
      currentStep: 'authorize',
      // currentStep: 'kinesis-setup',
      enabledSteps: ['authorize'],
      // enabledSteps: ['authorize', 'kinesis-setup'],
      formData: {
        /* Default Advanced Values */
        awsCloudWatchGlobalInput: '',
        awsCloudWatchAssumeARN: '',
        awsCloudWatchBatchSize: '10000',
        awsCloudWatchThrottleEnabled: '',
        awsCloudWatchThrottleWait: '1000',
        /* End Default Values */
        // awsCloudWatchName: 'test',
        // awsCloudWatchDescription: 'test',
        // awsCloudWatchAwsKey: '123',
        // awsCloudWatchAwsSecret: '123',
        // awsCloudWatchAwsRegion: 'us-east-2',
        // awsCloudWatchKinesisStream: 'stream-name-2',
      },
    };
  }

  _advancedOptions = () => {
    const { advOptionsOpened } = this.state;

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

  toggleAdvancedOptions = () => {
    const { advOptionsOpened } = this.state;

    this.setState({
      advOptionsOpened: !advOptionsOpened,
    });
  }

  render() {
    const { currentStep, formData } = this.state;
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
                                 advOptions={this._advancedOptions}
                                 hasStreams />),
        disabled: this.isDisabledStep('kinesis-setup'),
      },
      {
        key: 'health-check',
        title: 'AWS CloudWatch Health Check',
        component: (<StepHealthCheck onSubmit={this.handleSubmit} />),
        disabled: this.isDisabledStep('health-check'),
      },
      {
        key: 'review',
        title: 'AWS CloudWatch Review',
        component: (<StepReview onSubmit={this.handleSubmit} getAllValues={this.getAllFormData} />),
        disabled: this.isDisabledStep('review'),
      },
    ];
    this.availableSteps = wizardSteps.map(step => step.key);

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
