import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import { Button, Panel } from 'react-bootstrap';

export const ErrorMessage = ({ fullMessage, niceMessage }) => {
  const [expanded, toggleExpanded] = useState(false);
  if (!niceMessage) {
    return <Panel header={fullMessage} bsStyle="danger" />;
  }

  const Header = (
    <p>
      <span>{niceMessage}</span>
      <Button bsStyle="link"
              bsSize="xsmall"
              onClick={() => toggleExpanded(!expanded)}>More Info
      </Button>
    </p>
  );

  return (
    <Panel header={Header}
           bsStyle="danger"
           collapsible
           expanded={expanded}>
      <strong>Additional Information: </strong>{fullMessage}
    </Panel>
  );
};

ErrorMessage.propTypes = {
  fullMessage: PropTypes.string.isRequired,
  niceMessage: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
  ]),
};

ErrorMessage.defaultProps = {
  niceMessage: null,
};

const FormWrap = ({
  buttonContent,
  children,
  disabled,
  description,
  error,
  loading,
  onSubmit,
  title,
}) => {
  const formRef = useRef();
  const [disabledButton, setDisabledButton] = useState(disabled);
  const prevent = (event) => {
    event.preventDefault();
    return false;
  };

  useEffect(() => {
    setDisabledButton(loading || disabled);
  }, [loading, disabled]);

  return (
    <form onSubmit={prevent}
          autoComplete="off"
          noValidate
          ref={formRef}>

      {title && ((typeof (title) === 'string') ? <h2>{title}</h2> : title)}
      {description && ((typeof (description) === 'string') ? <p>{description}</p> : description)}

      {error && error.full_message && (
        <ErrorMessage fullMessage={error.full_message}
                      niceMessage={error.nice_message} />
      )}

      {children}

      <Button type="button"
              onClick={disabledButton ? null : onSubmit}
              bsStyle="primary"
              disabled={disabledButton}>
        {loading ? 'Loading...' : buttonContent}
      </Button>
    </form>
  );
};

FormWrap.propTypes = {
  buttonContent: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
  ]),
  children: PropTypes.any.isRequired,
  disabled: PropTypes.bool,
  error: PropTypes.shape({
    full_message: PropTypes.string.isRequired,
    nice_message: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.node,
    ]),
  }),
  description: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
  ]),
  loading: PropTypes.bool,
  onSubmit: PropTypes.func,
  title: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
  ]),
};

FormWrap.defaultProps = {
  buttonContent: 'Submit',
  disabled: true,
  error: null,
  description: null,
  loading: false,
  onSubmit: () => {},
  title: null,
};

export default FormWrap;
