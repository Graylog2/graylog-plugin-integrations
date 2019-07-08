import React from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';

import formValidation from '../utils/formValidation';

let currentForm;

const FormWrap = ({ children, buttonContent, onSubmit }) => {
  const handleSubmit = (event) => {
    if (formValidation.isFormValid(currentForm)) {
      onSubmit(event);
    }
  };

  const prevent = (event) => {
    event.preventDefault();
    return false;
  };

  return (
    <form onSubmit={prevent}
          autoComplete="off"
          ref={(form) => { currentForm = form; }}>
      {children}

      <Button type="button"
              onClick={handleSubmit}
              bsStyle="primary">
        {buttonContent}
      </Button>
    </form>
  );
};

FormWrap.propTypes = {
  children: PropTypes.any.isRequired,
  onSubmit: PropTypes.func,
  buttonContent: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
  ]),
};

FormWrap.defaultProps = {
  onSubmit: () => {},
  buttonContent: 'Submit',
};

export default FormWrap;
