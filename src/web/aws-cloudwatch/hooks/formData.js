import { useContext } from 'react';

import { FormDataContext } from '../reducers/FormDataContext';
import { UPDATE_FORM_DATA } from '../../common/constants';

const formDataHook = () => {
  const { state: { formData }, dispatch } = useContext(FormDataContext);

  const getFormData = () => formData;

  const setFormData = (id, updatedFormData) => {
    if (!id) {
      // eslint-disable-next-line
      console.warn('setFormData Hook requires `id`.');
      return false;
    }

    return dispatch({
      type: UPDATE_FORM_DATA,
      value: { id, ...updatedFormData },
    });
  };

  return {
    getFormData,
    setFormData,
  };
};

export default formDataHook;
