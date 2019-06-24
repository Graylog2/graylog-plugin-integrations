
import React, { createContext, useState, useContext } from 'react';
import PropTypes from 'prop-types';

import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

import { FormDataContext } from './FormData';

export const LogOutputContext = createContext();

export const LogOutputProvider = ({ children }) => {
  const { formData } = useContext(FormDataContext);
  const [logOutput, setLogState] = useState('');

  const setLogOutput = () => {
    const url = URLUtils.qualifyUrl('/plugins/org.graylog.integrations/aws/kinesis/healthCheck');

    const key = formData.awsCloudWatchAwsKey.value;
    const secret = formData.awsCloudWatchAwsSecret.value;
    const region = formData.awsCloudWatchAwsRegion.value;
    const stream = formData.awsCloudWatchKinesisStream.value;

    return fetch('POST', url, {
      region,
      aws_access_key_id: key,
      aws_secret_access_key: secret,
      stream_name: stream,
    }).then((response) => {
      setLogState(JSON.stringify(response.message_summary, null, 2));

      return response;
    });
  };

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
