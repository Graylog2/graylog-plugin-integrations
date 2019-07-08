import React, { useContext, useState } from 'react';

import Wizard from 'components/common/Wizard';

import StepAuthorize from './StepAuthorize';
import StepKinesis from './StepKinesis';
import StepHealthCheck from './StepHealthCheck';
import StepReview from './StepReview';

import { StepsContext } from './context/Steps';
import { FormDataContext } from './context/FormData';
import { LogOutputContext } from './context/LogOutput';
import TEMPORARY_LOG from './context/temporary_log';

const CloudWatch = () => {
  const {
    availableSteps,
    currentStep,
    isDisabledStep,
    setAvailableStep,
    setCurrentStep,
    setEnabledStep,
  } = useContext(StepsContext);
  const { setFormData } = useContext(FormDataContext);
  const { setLogOutput } = useContext(LogOutputContext);

  const handleStepChange = (nextStep) => {
    setCurrentStep(nextStep);
  };

  const handleEditClick = nextStep => () => {
    setCurrentStep(nextStep);
  };

  const handleFieldUpdate = ({ target }) => {
    const id = target.name || target.id;
    const value = Object.keys(target).includes('checked') ? target.checked : target.value;

    setFormData(id, { value });
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    const nextStep = availableSteps.indexOf(currentStep) + 1;

    if (availableSteps[nextStep]) {
      const key = availableSteps[nextStep];

      setLogOutput(TEMPORARY_LOG); // TODO: Move to step specific setting

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
      component: (<StepReview onSubmit={handleSubmit}
                              onEditClick={handleEditClick} />),
      disabled: isDisabledStep('review'),
    },
  ];

  if (availableSteps.length === 0) {
    setAvailableStep(wizardSteps.map(step => step.key));
  }

  return (
    <Wizard steps={wizardSteps}
            activeStep={currentStep}
            onStepChange={handleStepChange}
            horizontal
            justified
            hidePreviousNextButtons />
  );
};

export default CloudWatch;
