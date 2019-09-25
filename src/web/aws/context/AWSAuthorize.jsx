import React, { createContext, useState } from 'react';
import PropTypes from 'prop-types';

export const AWSAuthorizeContext = createContext();

const AUTH_TYPES = ['automatic', 'key_secret', 'arn'];

const AWSAuthorize = ({ children }) => {
  const [authType, setAuthType] = useState('automatic');

  const changeAuthType = (type) => {
    if (!AUTH_TYPES.find(type)) {
      // eslint-disable-next-line no-console
      console.error('Invalid AWS authorization type supplied.');
      return false;
    }

    setAuthType(type);
    return true;
  };

  return (
    <AWSAuthorizeContext.Provider value={{ authType, changeAuthType }}>
      {children}
    </AWSAuthorizeContext.Provider>
  );
};

AWSAuthorize.propTypes = {
  children: PropTypes.any.isRequired,
};

export default AWSAuthorize;
