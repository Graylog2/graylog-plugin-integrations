
import React, { createContext, useState, useContext } from 'react';
import PropTypes from 'prop-types';

import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

import { FormDataContext } from './FormData';

export const StreamsContext = createContext();

export const StreamsProvider = ({ children }) => {
  const [availableStreams, setStreamsState] = useState([]);

  const setStreams = () => {
    const { formData } = useContext(FormDataContext);

    const url = URLUtils.qualifyUrl('/plugins/org.graylog.integrations/aws/kinesis/streams');

    const key = formData.awsCloudWatchAwsKey.value;
    const secret = formData.awsCloudWatchAwsSecret.value;
    const region = formData.awsCloudWatchAwsRegion.value;

    return fetch('POST', url, {
      region,
      aws_access_key_id: key,
      aws_secret_access_key: secret,
    }).then((response) => {
      setStreamsState(response);

      return response;
    });
  };

  return (
    <StreamsContext.Provider value={{
      availableStreams,
      setStreams,
    }}>
      {children}
    </StreamsContext.Provider>
  );
};

StreamsProvider.propTypes = {
  children: PropTypes.any.isRequired,
};
