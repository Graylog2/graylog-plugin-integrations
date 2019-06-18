import React from 'react';

import Wizard from 'components/common/Wizard';

import StepAuthorize from './StepAuthorize';
import StepKinesis from './StepKinesis';
import StepHealthCheck from './StepHealthCheck';
import StepReview from './StepReview';

import stepsHook from './hooks/steps';
import formDataHook from './hooks/formData';
import advancedOptionsHook from './hooks/advancedOptions';

const CloudWatch = () => {
  const {
    isDisabledStep,
    getCurrentStep,
    setCurrentStep,
    setEnabledStep,
    getAvailableSteps,
    setAvailableSteps,
  } = stepsHook();
  const { setFormData } = formDataHook();
  const { toggleAdvancedOptionsVisiblity } = advancedOptionsHook();

  const handleStepChange = (currentStep) => {
    setCurrentStep(currentStep);
  };

  const handleEditClick = nextStep => () => {
    setCurrentStep(nextStep);
  };

  const handleFieldUpdate = ({ target }) => {
    const isChecked = Object.keys(target).includes('checked');
    const id = target.name || target.id;
    let { value } = target;

    if (isChecked) {
      value = target.checked ? value : '';
    }

    setFormData({ id, value });
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    const availableSteps = getAvailableSteps();
    const nextStep = availableSteps.indexOf(getCurrentStep()) + 1;

    if (availableSteps[nextStep]) {
      const key = availableSteps[nextStep];

      setCurrentStep(key);
      setEnabledStep(key);
    }
  };

  const wizardSteps = [
    {
      key: 'authorize',
      title: 'AWS CloudWatch Authorize',
      component: (<StepAuthorize onSubmit={handleSubmit} onChange={handleFieldUpdate} />),
      disabled: isDisabledStep('authorize'),
    },
    {
      key: 'kinesis-setup',
      title: 'AWS CloudWatch Kinesis Setup',
      component: (<StepKinesis onSubmit={handleSubmit}
                               onChange={handleFieldUpdate}
                               toggleAdvancedOptions={toggleAdvancedOptionsVisiblity}
                               hasStreams />),
      disabled: isDisabledStep('kinesis-setup'),
    },
    {
      key: 'health-check',
      title: 'AWS CloudWatch Health Check',
      component: (<StepHealthCheck onSubmit={handleSubmit} />),
      disabled: isDisabledStep('health-check'),
    },
    {
      key: 'review',
      title: 'AWS CloudWatch Review',
      component: (<StepReview onSubmit={handleSubmit} onEditClick={handleEditClick} />),
      disabled: isDisabledStep('review'),
    },
  ];

  if (getAvailableSteps().length === 0) {
    setAvailableSteps(wizardSteps.map(step => step.key));
  }

  return (
    <Wizard steps={wizardSteps}
            activeStep={getCurrentStep()}
            onStepChange={handleStepChange}
            horizontal
            justified
            hidePreviousNextButtons />
  );
};

export default CloudWatch;
