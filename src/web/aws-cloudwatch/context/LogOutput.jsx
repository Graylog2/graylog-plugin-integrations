
import React, { createContext, useState } from 'react';
import PropTypes from 'prop-types';

export const LogOutputContext = createContext();

export const LogOutputProvider = ({ children }) => {
  const [logOutput, setState] = useState();
  const setLogOutput = log => setState(JSON.stringify(log, null, 2));

  return (
    <LogOutputContext.Provider value={{
      logOutput,
      setLogOutput,
    }}>
      {children}
    </LogOutputContext.Provider>
  );
};

LogOutputProvider.propTypes = {
  children: PropTypes.any.isRequired,
};
