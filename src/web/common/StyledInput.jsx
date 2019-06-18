import React from 'react';
import PropTypes from 'prop-types';
import styled from '@emotion/styled';

import { Input } from 'components/bootstrap';

import formDataHook from '../aws-cloudwatch/hooks/formData';
import formValidation from '../utils/formValidation';

export default function StyledInput({ help, required, onChange, id: inputId, ...restProps }) {
  const { getFieldData } = formDataHook();
  const { error: hasError, errorMessage } = getFieldData(inputId);

  const checkValidity = (event) => {
    const errorOutput = formValidation.checkInputValidity(event.target, errorMessage);

    onChange(event, { error: !!errorOutput, errorMessage: errorOutput });
  };

  console.log('StyledInput', inputId, restProps);

  return (
    <React.Fragment>
      <Input {...restProps}
             id={inputId}
             onChange={checkValidity}
             bsStyle={required ? (hasError && 'error') : null}
             required={required}
             help={(required && <RequiredText>{errorMessage}</RequiredText>) || help} />

    </React.Fragment>
  );
}

StyledInput.propTypes = {
  onChange: PropTypes.func,
  id: PropTypes.string.isRequired,
  required: PropTypes.bool,
  help: PropTypes.array,
};

StyledInput.defaultProps = {
  onChange: () => {},
  required: false,
  help: null,
};

const RequiredText = styled.span`
  color: rgba(173, 7, 7, 0.7);
`;
