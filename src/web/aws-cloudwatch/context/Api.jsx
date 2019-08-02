import React, { createContext, useState } from 'react';
import PropTypes from 'prop-types';

export const ApiContext = createContext();

export const ApiProvider = ({ children }) => {
  const [availableRegions, setRegionsState] = useState([]);
  const [availableStreams, setStreamsState] = useState([]);
  const [logData, setLogDataState] = useState(null);

  const setRegions = results => setRegionsState(results.regions);

  const setStreams = (results) => {
    const streams = results.streams.map(stream => ({ value: stream, label: stream }));
    setStreamsState(streams);
  };

  const setLogData = (response) => {
    setLogDataState({
      message: JSON.stringify(response.message_fields, null, 2),
      type: response.input_type,
      additional: response.explanation,
    });
  };

  return (
    <ApiContext.Provider value={{
      availableStreams,
      setStreams,
      availableRegions,
      setRegions,
      logData,
      setLogData,
    }}>
      {children}
    </ApiContext.Provider>
  );
};

ApiProvider.propTypes = {
  children: PropTypes.any.isRequired,
};
