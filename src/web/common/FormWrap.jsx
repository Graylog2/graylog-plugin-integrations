import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';

const FormWrap = ({ children, disabled, buttonContent, loading, onSubmit, required, context }) => {
  const formRef = useRef();
  const [disabledButton, setDisabledButton] = useState(true);
  const prevent = (event) => {
    event.preventDefault();
    return false;
  };

  useEffect(() => {
    const missingValue = required.find(field => !context[field] || !context[field].value);
    setDisabledButton(loading || disabled || !!missingValue);
  }, [loading, disabled, context]);

  return (
    <form onSubmit={prevent}
          autoComplete="off"
          noValidate
          ref={formRef}>

      {children}

      <Button type="button"
              onClick={onSubmit}
              bsStyle="primary"
              disabled={disabledButton}>
        {loading ? 'Loading...' : buttonContent}
      </Button>
    </form>
  );
};

FormWrap.propTypes = {
  children: PropTypes.any.isRequired,
  onSubmit: PropTypes.func,
  loading: PropTypes.bool,
  disabled: PropTypes.bool,
  buttonContent: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
  ]),
  required: PropTypes.arrayOf(PropTypes.string),
  context: PropTypes.object,
};

FormWrap.defaultProps = {
  onSubmit: () => {},
  buttonContent: 'Submit',
  loading: false,
  disabled: false,
  required: [],
  context: {},
};

export default FormWrap;
