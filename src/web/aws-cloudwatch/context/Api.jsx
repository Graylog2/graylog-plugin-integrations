import React, { createContext, useContext, useState } from 'react';
import PropTypes from 'prop-types';

import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

import { FormDataContext } from './FormData';
import { awsAuth } from './default_settings';

export const ApiContext = createContext();

export const ApiProvider = ({ children }) => {
  const [availableRegions, setRegionsState] = useState([]);
  const [availableStreams, setStreamsState] = useState([]);
  const [logSample, setLogSampleState] = useState('');

  const setRegions = () => {
    const url = URLUtils.qualifyUrl('/plugins/org.graylog.integrations/aws/regions');

    return fetch('GET', url).then(response => setRegionsState(response.regions));
  };

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
