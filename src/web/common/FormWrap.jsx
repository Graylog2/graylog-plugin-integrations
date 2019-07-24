import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';

const FormWrap = ({ children, buttonContent, loading, onSubmit, required, context }) => {
  const formRef = useRef();
  const [disabledButton, setDisabledButton] = useState(true);
  const prevent = (event) => {
    event.preventDefault();
    return false;
  };

  useEffect(() => {
    const missingValue = required.find(field => !context[field] || !context[field].value);
    const invalidForm = formRef.current && !formRef.current.checkValidity();

    setDisabledButton(loading || !!missingValue || invalidForm);
  }, [loading, context]);

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
  required: [],
  context: {},
};

export default FormWrap;
