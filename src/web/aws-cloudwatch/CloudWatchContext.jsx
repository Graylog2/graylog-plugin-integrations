import React, { createContext, useReducer } from 'react';
import PropTypes from 'prop-types';

import FIELDS from './utils';

const logOutput = { // Demo Data until API is wired
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

const CloudWatchContext = createContext();

const CloudWatchProvider = ({ children }) => {
  const initialState = {
    availableSteps: [],
    visibleAdvancedOptions: false,
    currentStep: 'authorize',
    enabledSteps: ['authorize'],
    formData: [
      ...FIELDS,
    ],
    logOutput: JSON.stringify(logOutput, null, 2),
  };

  const reducer = (state, action) => {
    switch (action.type) {
      case 'SET_CURRENT_STEP': {
        return {
          ...state,
          currentStep: action.value,
        };
      }

      case 'ADD_ENABLED_STEP': {
        return {
          ...state,
          enabledSteps: [...state.enabledSteps, action.value],
        };
      }

      case 'UPDATE_FORM_DATA': {
        const { formData } = state;
        const { value: { id, value } } = action;
        const existingFields = formData.map(field => field.id);
        let updatedFormData = formData;

        if (existingFields.includes(id)) { // check if it's already been set
          updatedFormData = formData.map((field) => {
            if (field.id === id) { // loop through and find the existing field
              return {
                ...field, // spread existing data and update values as necessary
                value: field.value !== value ? value : field.value,
              };
            }

            return field;
          });
        } else {
          // Add new field to formData
          updatedFormData = [...updatedFormData, { id, value }];
        }

        return {
          ...state,
          formData: updatedFormData,
        };
      }

      case 'SET_ADVANCED_VISIBILITY': {
        return {
          ...state,
          visibleAdvancedOptions: action.value,
        };
      }

      case 'SET_AVAILABLE_STEPS': {
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
    <CloudWatchContext.Provider value={{ state, dispatch }}>
      {children}
    </CloudWatchContext.Provider>
  );
};

CloudWatchProvider.propTypes = {
  children: PropTypes.any.isRequired,
};


export { CloudWatchContext, CloudWatchProvider };
