import { useContext } from 'react';
import _ from 'lodash';
import { CloudWatchContext } from '../CloudWatchContext';

const formDataHook = () => {
  const { state: { formData }, dispatch } = useContext(CloudWatchContext);

  const getFormData = () => {
    return formData;
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

  const setFormData = ({ id, value }) => dispatch({
    type: 'UPDATE_FORM_DATA',
    value: { id, value },
  });

  return {
    getFormData,
    setFormData,
    getFieldData,
    getFieldValue,
  };
};

export default formDataHook;
