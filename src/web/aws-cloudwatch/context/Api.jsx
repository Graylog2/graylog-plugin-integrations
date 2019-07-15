import React, { createContext, useState } from 'react';
import PropTypes from 'prop-types';

export const ApiContext = createContext();

export const ApiProvider = ({ children }) => {
  const [availableRegions, setRegionsState] = useState([]);
  const [availableStreams, setStreamsState] = useState([]);
  const [logSample, setLogSampleState] = useState('');

  const setRegions = results => setRegionsState(results.regions);

  const setStreams = (results) => {
    const streams = results.streams.map(stream => ({ value: stream, label: stream }));
    setStreamsState(streams);
  };

  const setLogSample = (response) => {
    setLogSampleState(JSON.stringify(response.message_fields, null, 2));
  };

  return (
    <ApiContext.Provider value={{
      availableStreams,
      setStreams,
      availableRegions,
      setRegions,
      logSample,
      setLogSample,
    }}>
      {children}
    </ApiContext.Provider>
  );
};

ApiProvider.propTypes = {
  children: PropTypes.any.isRequired,
};
