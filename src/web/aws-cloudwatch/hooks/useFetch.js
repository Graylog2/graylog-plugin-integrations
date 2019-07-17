import { useContext, useEffect, useState } from 'react';

import URLUtils from 'util/URLUtils';
import fetch from 'logic/rest/FetchProvider';

import { FormDataContext } from '../context/FormData';
import { awsAuth } from '../context/default_settings';

/* useFetch Custom Hook

Because of [Rules of Hooks](https://reactjs.org/docs/hooks-rules.html) we have to get fancy
in order to use fetch Promises and hooks.

PARAMETERS:
 - `setHook` that you want to call after fetch [required]
 - `method`: '', [`'GET'` optional]
 - `options`: '', [`{}` optional]
 - `callback` that will fire any part of your code you need after fetch [`() => {}` optional]

USE:
`const [status, setUrl] = useFetch(setNextHook, onFooBar);`
 - `status` will provide the current reducer state `{ loading, error, data }`
 - `setUrl` will be your hook to call as a submit handler within a subfunction, it needs the API route as a string:

EXAMPLES:
 - See uses in `src/web/aws-cloudwatch/StepAuthorize.jsx`
*/

const useFetch = (setHook, method = 'GET', options = {}, callback = () => {}) => {
  const { formData } = useContext(FormDataContext);
  const [fetchUrl, setFetchUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);
  const [data, setData] = useState(false);

  const qualifiedURL = fetchUrl ? URLUtils.qualifyUrl(fetchUrl) : fetchUrl;

  useEffect(() => {
    let didCancel = !qualifiedURL;
    let result;

    const fetchData = async () => {
      try {
        if (qualifiedURL && !didCancel) {
          setLoading(true);

          if (method === 'GET') {
            result = await fetch(method, qualifiedURL);
          } else {
            const { key, secret } = awsAuth(formData);
            result = await fetch(method, qualifiedURL, {
              aws_access_key_id: key,
              aws_secret_access_key: secret,
              ...options,
            });
          }

          setLoading(false);
          setData(result);
          setHook(result);
          callback();
        }
      } catch (err) {
        if (!didCancel) {
          setLoading(false);
          setError(err);
        }
      }
    };

    fetchData();

    return () => {
      didCancel = true;
    };
  }, [qualifiedURL]);

  return [{ loading, error, data }, setFetchUrl];
};

export default useFetch;
