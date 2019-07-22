import React, { useContext, useState } from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router';

import ConfirmLeaveDialog from 'components/common/ConfirmLeaveDialog';

import Wizard from 'components/common/Wizard';
import FormUtils from 'util/FormsUtils.js';
import history from 'util/History';
import Routes from 'routing/Routes';

import StepAuthorize from './StepAuthorize';
import StepKinesis from './StepKinesis';
import StepHealthCheck from './StepHealthCheck';
import StepReview from './StepReview';
import { StepsContext } from './context/Steps';
import { FormDataContext } from './context/FormData';

const CloudWatch = ({ route }) => {
  const {
    availableSteps,
    currentStep,
    isDisabledStep,
    setAvailableStep,
    setCurrentStep,
    setEnabledStep,
  } = useContext(StepsContext);
  const { setFormData } = useContext(FormDataContext);
  const [dirty, setDirty] = useState(false);

  const handleStepChange = (nextStep) => {
    setCurrentStep(nextStep);
  };

  const handleEditClick = nextStep => () => {
    setCurrentStep(nextStep);
  };

  const handleFieldUpdate = ({ target }, fieldData) => {
    const id = target.name || target.id;
    const value = FormUtils.getValueFromInput(target);

    if (!dirty) {
      setDirty(true);
    }

    setFormData(id, { ...fieldData, value });
  };

  const handleSubmit = () => {
    // TODO: add String.trim() to inputs
    const nextStep = availableSteps.indexOf(currentStep) + 1;

    if (availableSteps[nextStep]) {
      const key = availableSteps[nextStep];

      setCurrentStep(key);
      setEnabledStep(key);
    } else {
      history.push(Routes.SYSTEM.INPUTS);
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
    <>
      {dirty && <ConfirmLeaveDialog route={route} question="Are you sure? Your new Input will not be created." />}
      <Wizard steps={wizardSteps}
              activeStep={currentStep}
              onStepChange={handleStepChange}
              horizontal
              justified
              hidePreviousNextButtons />
    </>
  );
};

CloudWatch.propTypes = {
  route: PropTypes.object.isRequired,
};

export default withRouter(CloudWatch);
