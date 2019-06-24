import React, { useContext } from 'react';

import Wizard from 'components/common/Wizard';
import FormUtils from 'util/FormsUtils.js';
import formValidation from 'utils/formValidation';

import StepAuthorize from './StepAuthorize';
import StepKinesis from './StepKinesis';
import StepHealthCheck from './StepHealthCheck';
import StepReview from './StepReview';
import { StepsContext } from './context/Steps';
import { FormDataContext } from './context/FormData';

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

  const handleStepChange = (nextStep) => {
    setCurrentStep(nextStep);
  };

  const handleEditClick = nextStep => () => {
    setCurrentStep(nextStep);
  };

  const handleFieldUpdate = ({ target }, fieldData) => {
    const id = target.name || target.id;
    const value = FormUtils.getValueFromInput(target);

    setFormData(id, { ...fieldData, value });
  };

  const handleSubmit = (event, form) => {
    // TODO: add String.trim() to inputs
    if (!event && form) {
      const formElements = Array.from(form.elements);

      formElements.forEach((field) => {
        const errorOutput = formValidation.checkInputValidity(field);

        if (field.id && errorOutput) {
          setFormData(field.id, { error: true });
        }
      });

      return false;
    }

    event.preventDefault();

    const nextStep = availableSteps.indexOf(currentStep) + 1;

    if (availableSteps[nextStep]) {
      const key = availableSteps[nextStep];

      setCurrentStep(key);
      setEnabledStep(key);
    }

    return false;
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
