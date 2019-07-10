import React from 'react';
import PropTypes from 'prop-types';

import { Input } from 'components/bootstrap';

import formValidation from '../utils/formValidation';

const ValidatedInput = ({ help, required, onChange, id, fieldData, ...restProps }) => {
  const { dirty, error, value } = fieldData;

  const handleChange = (event) => {
    onChange(event, { dirty: true });
  };

  const checkValidity = (event) => {
    if (dirty) {
      const errorOutput = formValidation.checkInputValidity(event.target);

      onChange(event, { error: !!errorOutput });
    }
  };

  return (
    <Input {...restProps}
           id={id}
           onChange={handleChange}
           onBlur={checkValidity}
           bsStyle={(error && dirty && 'error') || null}
           required={required}
           defaultValue={value}
           help={help} />
  );
};

ValidatedInput.propTypes = {
  onChange: PropTypes.func,
  id: PropTypes.string.isRequired,
  required: PropTypes.bool,
  help: PropTypes.string,
  fieldData: PropTypes.shape({
    error: PropTypes.bool,
    dirty: PropTypes.bool,
    value: PropTypes.string,
  }),
};

ValidatedInput.defaultProps = {
  onChange: () => {},
  required: false,
  help: '',
  fieldData: {
    dirty: false,
    error: false,
    value: '',
  },
};

export default ValidatedInput;
