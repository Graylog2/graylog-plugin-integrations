import { useReducer, useContext, useEffect, useState } from 'react';

import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

import fetchReducer from './fetchReducer';

import { FormDataContext } from '../context/FormData';
import { awsAuth } from '../context/default_settings';

/* useFetch Custom Hook

Because of [Rules of Hooks](https://reactjs.org/docs/hooks-rules.html) we have to get fancy
in order to use fetch Promises and hooks.

This function defaults to:
{
    method: 'GET',
    options: {},
    url: '',
}

It accepts two callbacks:
 - the `setHook` that you want to call after fetch
 - the `callback` that will fire any part of your code you need after fetch
both default to `() => {}` so neither are required

USE:
`const [status, setOptions] = useFetch(setNextHook, onFooBar);`
 - `status` will provide the current reducer state, including `isLoading` and `isError`
 - `setOptions` will be your hook to call as a submit handler within a subfunction

EXAMPLES: See uses in `src/web/aws-cloudwatch/StepAuthorize.jsx`
*/

const useFetch = (setHook = () => {}, callback = () => {}) => {
  const { formData } = useContext(FormDataContext);
  const [fetchOptions, setFetchOptions] = useState({
    method: 'GET',
    options: {},
    url: '',
  });
  const [state, dispatch] = useReducer(fetchReducer, {
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
