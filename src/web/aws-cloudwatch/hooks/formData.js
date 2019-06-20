import { useContext } from 'react';
import _ from 'lodash';
import { CloudWatchContext } from '../CloudWatchContext';

const formDataHook = () => {
  const { state: { formData }, dispatch } = useContext(CloudWatchContext);

  const getFormData = () => {
    return formData;
  };

  const setFormData = (id, { value, error = false, dirty = true }) => {
    if (!id) {
      // eslint-disable-next-line
      console.warn('setFormData Hook requires `id`.');
      return false;
    }

    return dispatch({
      type: 'SET_FIELD_VALUE',
      value: { id, value, error, dirty },
    });
  };

  const getFieldData = (field) => {
    const fieldData = _.find(formData, fields => fields.id === field);

    return fieldData || { id: 'NotFound', value: '', error: true };
  };

  const getFieldValue = (field) => {
    const fieldData = getFieldData(field);
    const defaultValue = fieldData ? fieldData.defaultValue : '';
    const currentValue = fieldData ? fieldData.value : '';

    return currentValue || defaultValue;
  };

  return {
    getFormData,
    getFieldData,
    getFieldValue,
    setFormData,
  };
};

export default formDataHook;
