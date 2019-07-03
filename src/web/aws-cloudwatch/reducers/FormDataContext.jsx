import React, { createContext, useReducer } from 'react';
import PropTypes from 'prop-types';

import { UPDATE_FORM_DATA } from '../../common/constants';

const FormDataContext = createContext();

const FormDataProvider = ({ children }) => {
  const initialState = {
    formData: {
      /*
      fieldId: { // Same ID as supplied to <Input />
        value: '',
        defaultValue: '', // Update StepReview.jsx & relevant step(s) if you need to output
      }
      */

      /* Default Advanced Settings */
      awsCloudWatchBatchSize: {
        defaultValue: '10000',
      },
      awsCloudWatchThrottleWait: {
        defaultValue: '1000',
      },

      /* Test Settings */
      // TODO: Remove these before any official launch, but I'm tired of copy/paste during dev
      awsCloudWatchName: {
        value: 'Name',
      },
      awsCloudWatchDescription: {
        value: 'Description',
      },
      awsCloudWatchAwsKey: {
        value: 'AKQQQQQQQQQQQQQQQQQQ',
      },
      awsCloudWatchAwsSecret: {
        value: 'XfoodLP3YXdzX3NlY3JldF8wMDNOTDAwMDbeMA==',
      },
      awsCloudWatchAwsRegion: {
        value: 'us-east-2',
      },
      /* End Test Settings */
    },
  };

  const reducer = (state, action) => {
    switch (action.type) {
      case UPDATE_FORM_DATA: {
        const { formData } = state;
        const { value: { id, value } } = action;

        const updatedFormData = {
          ...formData,
          [id]: {
            ...formData[id],
            value,
          },
        };

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
