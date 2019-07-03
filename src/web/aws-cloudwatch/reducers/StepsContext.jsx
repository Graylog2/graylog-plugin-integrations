
import React, { createContext, useReducer } from 'react';
import PropTypes from 'prop-types';

import {
  ADD_ENABLED_STEP,
  SET_CURRENT_STEP,
  SET_AVAILABLE_STEPS,
} from '../../common/constants';

const StepsContext = createContext();

const StepsProvider = ({ children }) => {
  const initialState = {
    availableSteps: [],
    currentStep: 'authorize',
    enabledSteps: ['authorize'],
  };

  const reducer = (state, action) => {
    switch (action.type) {
      case SET_CURRENT_STEP: {
        return {
          ...state,
          currentStep: action.value,
        };
      }

      case ADD_ENABLED_STEP: {
        return {
          ...state,
          enabledSteps: [...state.enabledSteps, action.value],
        };
      }

      case SET_AVAILABLE_STEPS: {
        return {
          ...state,
          availableSteps: action.value,
        };
      }

      default: return state;
    }
  };

  const [state, dispatch] = useReducer(reducer, initialState);

  return (
    <StepsContext.Provider value={{ state, dispatch }}>
      {children}
    </StepsContext.Provider>
  );
};

StepsProvider.propTypes = {
  children: PropTypes.any.isRequired,
};

export { StepsContext, StepsProvider };
