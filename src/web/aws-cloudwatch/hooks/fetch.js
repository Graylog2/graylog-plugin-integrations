import { useReducer, useContext, useEffect, useState } from 'react';

import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

import { FormDataContext } from '../context/FormData';
import { awsAuth } from '../context/default_settings';

const dataFetchReducer = (state, action) => {
  switch (action.type) {
    case 'FETCH_INIT':
      return { ...state, isLoading: true, isError: false };
    case 'FETCH_SUCCESS':
      return {
        ...state,
        isLoading: false,
        isError: false,
        data: action.payload,
      };
    case 'FETCH_FAILURE':
      return {
        ...state,
        isLoading: false,
        isError: true,
      };
    default:
      throw new Error();
  }
};

const useFetch = (setHook = () => {}, callback = () => {}) => {
  const { formData } = useContext(FormDataContext);
  const [fetchOptions, setFetchOptions] = useState({
    method: 'GET',
    options: {},
    url: '',
  });
  const [state, dispatch] = useReducer(dataFetchReducer, {
    isLoading: false,
    isError: false,
    data: [],
  });

  const qualifiedURL = fetchOptions.url ? URLUtils.qualifyUrl(fetchOptions.url) : fetchOptions.url;
  const { key, secret } = awsAuth(formData);

  useEffect(() => {
    let didCancel = !qualifiedURL;
    let result;

    const fetchData = async () => {
      try {
        if (qualifiedURL) {
          dispatch({ type: 'FETCH_INIT' });

          result = await fetch(fetchOptions.method, qualifiedURL, {
            aws_access_key_id: key,
            aws_secret_access_key: secret,
            ...fetchOptions.options,
          });
        }

        if (!didCancel) {
          dispatch({ type: 'FETCH_SUCCESS', payload: result });
          setHook(result);
          callback();
        }
      } catch (error) {
        if (!didCancel) {
          dispatch({ type: 'FETCH_FAILURE' });
        }
      }
    };

    fetchData();

    return () => {
      didCancel = true;
    };
  }, [qualifiedURL]);

  return [state, setFetchOptions];
};

export default useFetch;
