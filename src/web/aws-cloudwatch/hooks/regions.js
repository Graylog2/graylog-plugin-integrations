import { useContext } from 'react';

import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

import { ApiContext } from '../context/Api';

const regionsHook = () => {
  const { apiState: { regions }, dispatchApi } = useContext(ApiContext);

  const setRegions = () => {
    const url = URLUtils.qualifyUrl('/plugins/org.graylog.integrations/aws/regions');

    return fetch('GET', url).then((response) => {
      dispatchApi({
        type: 'SET_REGIONS',
        value: response.regions,
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
