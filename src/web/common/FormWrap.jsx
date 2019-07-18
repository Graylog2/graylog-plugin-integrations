import React, { useRef } from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';

const FormWrap = ({ children, buttonContent, loading, onSubmit }) => {
  const currentForm = useRef();
  const isDisabled = loading || (currentForm.current && !currentForm.current.checkValidity());
  const prevent = (event) => {
    event.preventDefault();
    return false;
  };

  return (
    <form onSubmit={prevent}
          autoComplete="off"
          noValidate
          ref={currentForm}>
      {children}

      <Button type="button"
              onClick={onSubmit}
              bsStyle="primary"
              disabled={isDisabled}>
        {loading ? 'Loading...' : buttonContent}
      </Button>
    </form>
  );
};

FormWrap.propTypes = {
  children: PropTypes.any.isRequired,
  onSubmit: PropTypes.func,
  loading: PropTypes.bool,
  buttonContent: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
  ]),
};

FormWrap.defaultProps = {
  onSubmit: () => {},
  buttonContent: 'Submit',
  loading: false,
};

export default FormWrap;
