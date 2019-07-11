import React, { createContext, useReducer } from 'react';
import PropTypes from 'prop-types';

export const ApiContext = createContext();

export const ApiProvider = ({ children }) => {
  const initialState = {
    regions: [],
    streams: [],
    logSample: '',
  };

  const reducer = (state, action) => {
    switch (action.type) {
      case 'SET_REGIONS': {
        return {
          ...state,
          regions: action.value,
        };
      }

      case 'SET_STREAMS': {
        return {
          ...state,
          streams: action.value,
        };
      }

      case 'SET_LOG': {
        return {
          ...state,
          logOutput: action.value,
        };
      }

      default: return state;
    }
  };

  const [apiState, dispatchApi] = useReducer(reducer, initialState);

  return (
    <ApiContext.Provider value={{ apiState, dispatchApi }}>
      {children}
    </ApiContext.Provider>
  );
};

ApiProvider.propTypes = {
  children: PropTypes.any.isRequired,
};
