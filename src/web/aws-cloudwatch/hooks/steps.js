import { useContext } from 'react';
import { StepsContext } from '../reducers/StepsContext';
import {
  ADD_ENABLED_STEP,
  SET_CURRENT_STEP,
  SET_AVAILABLE_STEPS,
} from '../../common/constants';

const stepsHook = () => {
  const { state: { availableSteps, currentStep, enabledSteps }, dispatch } = useContext(StepsContext);

  const isDisabledStep = (step) => {
    if (!enabledSteps || enabledSteps.length === 0) {
      return true;
    }

    return !enabledSteps.includes(step);
  };

  const setEnabledStep = step => dispatch({
    type: ADD_ENABLED_STEP,
    value: step,
  });

  const getCurrentStep = () => currentStep;

  const setCurrentStep = step => dispatch({
    type: SET_CURRENT_STEP,
    value: step,
  });

  const getAvailableSteps = () => availableSteps;

  const setAvailableSteps = steps => dispatch({
    type: SET_AVAILABLE_STEPS,
    value: steps,
  });


  return {
    getAvailableSteps,
    isDisabledStep,
    getCurrentStep,
    setCurrentStep,
    setEnabledStep,
    setAvailableSteps,
  };
};

export default stepsHook;
