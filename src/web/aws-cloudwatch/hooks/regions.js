import { useContext } from 'react';

import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

import { ApiContext } from '../context/Api';

const regionsHook = () => {
  const { apiState: { regions }, dispatchApi } = useContext(ApiContext);

  const setRegions = () => {
    console.log('setRegions hook');
    const url = URLUtils.qualifyUrl('/plugins/org.graylog.integrations/aws/regions');

    return fetch('GET', url).then((response) => {
      console.log('fetch resp', response);
      dispatchApi({
        type: 'SET_REGIONS',
        value: response,
      });
    });
  };

  const getRegions = () => {
    return regions;
  };

  return {
    getRegions,
    setRegions,
  };
};

export default regionsHook;
