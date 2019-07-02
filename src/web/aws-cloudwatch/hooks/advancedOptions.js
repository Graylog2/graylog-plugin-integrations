import { useContext } from 'react';
import { CloudWatchContext } from '../CloudWatchContext';

const advancedOptionsHook = () => {
  const { state: { visibleAdvancedOptions }, dispatch } = useContext(CloudWatchContext);

  const getAdvancedOptionsVisiblity = () => visibleAdvancedOptions;

  const toggleAdvancedOptionsVisiblity = () => dispatch({
    type: 'SET_ADVANCED_VISIBILITY',
    value: !visibleAdvancedOptions,
  });

  const setAdvancedOptionsVisiblity = visible => dispatch({
    type: 'SET_ADVANCED_VISIBILITY',
    value: visible,
  });

  return {
    getAdvancedOptionsVisiblity,
    toggleAdvancedOptionsVisiblity,
    setAdvancedOptionsVisiblity,
  };
};

export default advancedOptionsHook;
