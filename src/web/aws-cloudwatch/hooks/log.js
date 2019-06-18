import { useContext } from 'react';
import { CloudWatchContext } from '../CloudWatchContext';

const logHook = () => {
  const { state: { logOutput } } = useContext(CloudWatchContext);

  const getLog = () => {
    return logOutput;
  };

  return {
    getLog,
  };
};

export default logHook;
