
import React, { createContext, useState } from 'react';
import PropTypes from 'prop-types';

import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

export const RegionsContext = createContext();

export const RegionsProvider = ({ children }) => {
  const [availableRegions, setRegionsState] = useState([]);

  const setRegions = () => {
    const url = URLUtils.qualifyUrl('/plugins/org.graylog.integrations/aws/regions');

    return fetch('GET', url).then(response => setRegionsState(response));
  };

  return (
    <RegionsContext.Provider value={{
      availableRegions,
      setRegions,
    }}>
      {children}
    </RegionsContext.Provider>
  );
};

RegionsProvider.propTypes = {
  children: PropTypes.any.isRequired,
};
