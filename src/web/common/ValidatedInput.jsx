import React from 'react';
import PropTypes from 'prop-types';
import styled from '@emotion/styled';

import { Input } from 'components/bootstrap';

import formValidation from '../utils/formValidation';

const Label = ({ label, error }) => {
  if (error) {
    return (
      <React.Fragment>
        {label}
        <Error><i className="fa fa-exclamation-triangle" /> {error}</Error>
      </React.Fragment>
    );
  }

  return label;
};

const ValidatedInput = ({ help, onChange, id, label, fieldData, ...restProps }) => {
  const { dirty, error, value } = fieldData;

  const handleChange = (event) => {
    onChange(event, { dirty: true });
  };

  const checkValidity = (event) => {
    if (dirty) {
      const errorOutput = formValidation.checkInputValidity(event.target);

      onChange(event, { error: errorOutput });
    }
  };

  return (
    <Input {...restProps}
           id={id}
           onChange={handleChange}
           onBlur={checkValidity}
           bsStyle={(error && dirty && 'error') || null}
           value={value}
           label={<Label label={label} error={error} />}
           help={help} />
  );
};

ValidatedInput.propTypes = {
  fieldData: PropTypes.shape({
    error: PropTypes.string,
    dirty: PropTypes.bool,
    value: PropTypes.string,
  }),
  help: PropTypes.string,
  label: PropTypes.string.isRequired,
  id: PropTypes.string.isRequired,
  onChange: PropTypes.func,
  required: PropTypes.bool,
};

ValidatedInput.defaultProps = {
  onChange: () => {},
  required: false,
  help: '',
  fieldData: {
    dirty: false,
    error: undefined,
    value: undefined,
  },
};

const Error = styled.span`
  display: block;
  font-weight: normal;
`;

export default ValidatedInput;
