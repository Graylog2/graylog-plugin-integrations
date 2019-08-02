import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import { Button, Panel } from 'react-bootstrap';
import styled, { createGlobalStyle } from 'styled-components';

export const ErrorMessage = ({ fullMessage, niceMessage }) => {
  const [expanded, toggleExpanded] = useState(false);

  const Header = (
    <>
      <ErrorOutputStyle />
      <ErrorOutput>{niceMessage || fullMessage}</ErrorOutput>
      {niceMessage
        && (
          <ErrorToggleInfo onClick={() => toggleExpanded(!expanded)} expanded={expanded}>
            More Info <i className="fa fa-chevron-right" />
          </ErrorToggleInfo>
        )
      }
    </>
  );

  if (!niceMessage) {
    return <Panel header={Header} bsStyle="danger" />;
  }

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

const ErrorOutputStyle = createGlobalStyle`
  /* NOTE: This is to remove Bootstrap styles from the anchor element I can't override in Panel.Header */
  form {
    .panel.panel-danger {
      .panel-heading > a {
        font-size: 14px;
        text-decoration: none;
        color: #AD0707;

        :hover {
          text-decoration: none;
        }
      }
    }
  }
`;

const ErrorOutput = styled.span`
  display: block;
`;

const ErrorToggleInfo = styled.button`
  border: 0;
  background: none;
  color: #1F1F1F;
  font-size: 11px;
  text-transform: uppercase;
  margin: 12px 0 0;
  padding: 0;

  .fa {
    transform: rotate(${props => (props.expanded ? '90deg' : '0deg')});
    transition: 150ms transform ease-in-out;
  }
`;

export default FormWrap;
