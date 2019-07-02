import { useContext } from 'react';
import _ from 'lodash';
import { CloudWatchContext } from '../CloudWatchContext';

const formDataHook = () => {
  const { state: { formData }, dispatch } = useContext(CloudWatchContext);

  const getFormData = () => formData;

  const getFieldData = (field) => {
    const fieldData = _.find(formData, fields => fields.id === field);
    const errorResponse = {
      id: 'NotFound',
      value: '',
      error: true,
      errorMessage: `Unable to find field ${field}`,
    };

    return fieldData || errorResponse;
  };

  const getFieldValue = (field) => {
    const fieldData = getFieldData(field);
    const defaultValue = fieldData ? fieldData.defaultValue : '';
    const currentValue = fieldData ? fieldData.value : '';

    return currentValue || defaultValue;
  };

  const setFormData = (id, updatedFormData) => {
    if (!id) {
      // eslint-disable-next-line
      console.warn('setFormData Hook requires `id`.');
      return false;
    }

    return dispatch({
      type: 'UPDATE_FORM_DATA',
      value: { id, ...updatedFormData },
    });
  };

  return {
    getFormData,
    setFormData,
    getFieldData,
    getFieldValue,
  };
};

export default formDataHook;
