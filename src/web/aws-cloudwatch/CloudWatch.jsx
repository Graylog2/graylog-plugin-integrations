import React, { useContext, useState } from 'react';

import Wizard from 'components/common/Wizard';

import StepAuthorize from './StepAuthorize';
import StepKinesis from './StepKinesis';
import StepHealthCheck from './StepHealthCheck';
import StepReview from './StepReview';

import { StepsContext } from './providers/Steps';
import { FormDataContext } from './providers/FormData';

const exampleLogs = { // TODO: Demo Data until API is wired
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
};

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

  const [isAdvancedOptionsVisible, setAdvancedOptionsVisiblity] = useState(false);
  const [logOutput, setLogOutput] = useState('');


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

      setLogOutput(JSON.stringify(exampleLogs, null, 2)); // TODO: Move to step specific setting

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
                               isAdvancedOptionsVisible={isAdvancedOptionsVisible}
                               setAdvancedOptionsVisiblity={setAdvancedOptionsVisiblity}
                               hasStreams />),
      disabled: isDisabledStep('kinesis-setup'),
    },
    {
      key: 'health-check',
      title: 'AWS CloudWatch Health Check',
      component: (<StepHealthCheck onSubmit={handleSubmit} logOutput={logOutput} />),
      disabled: isDisabledStep('health-check'),
    },
    {
      key: 'review',
      title: 'AWS CloudWatch Review',
      component: (<StepReview onSubmit={handleSubmit}
                              onEditClick={handleEditClick}
                              logOutput={logOutput} />),
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
