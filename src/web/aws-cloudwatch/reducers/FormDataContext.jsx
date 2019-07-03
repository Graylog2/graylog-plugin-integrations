import React, { createContext, useReducer } from 'react';
import PropTypes from 'prop-types';

import { UPDATE_FORM_DATA } from '../../common/constants';

const FormDataContext = createContext();

const FormDataProvider = ({ children }) => {
  const initialState = {
    formData: [
      /*
      Available options
        {
          id: Reference used in API transaction [required]
          defaultValue: Value that will render as default
        }
      */
      /* Default Advanced Values */
      {
        id: 'awsCloudWatchGlobalInput',
        defaultValue: '',
      },
      {
        id: 'awsCloudWatchAssumeARN',
        defaultValue: '',
      },
      {
        id: 'awsCloudWatchBatchSize',
        defaultValue: '10000',
      },
      {
        id: 'awsCloudWatchThrottleEnabled',
        defaultValue: '',
      },
      {
        id: 'awsCloudWatchThrottleWait',
        defaultValue: '1000',
      },
    ],
  };

  const reducer = (state, action) => {
    switch (action.type) {
      case UPDATE_FORM_DATA: {
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

      default: return state;
    }
  };

  const [state, dispatch] = useReducer(reducer, initialState);

  return (
    <FormDataContext.Provider value={{ state, dispatch }}>
      {children}
    </FormDataContext.Provider>
  );
};

FormDataProvider.propTypes = {
  children: PropTypes.any.isRequired,
};

export { FormDataContext, FormDataProvider };
