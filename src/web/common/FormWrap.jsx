import React from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';

let currentForm;
const FormWrap = ({ children, buttonContent, onSubmit }) => {
  const isValid = currentForm && currentForm.checkValidity();
  const prevent = (event) => {
    event.preventDefault();
    return false;
  };

  return (
    <form onSubmit={prevent}
          autoComplete="off"
          noValidate
          ref={(form) => { currentForm = form; }}>
      {children}

      <Button type="button"
              onClick={onSubmit}
              bsStyle="primary"
              disabled={!isValid}>
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
