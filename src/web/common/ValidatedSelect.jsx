import React from 'react';
import PropTypes from 'prop-types';
import { ControlLabel, FormGroup, HelpBlock } from 'react-bootstrap';
import { components } from 'react-select';

import { Select } from 'components/common';

const Placeholder = (props) => {
  return <components.Placeholder {...props} />;
};

const ValidatedSelect = ({ id, onChange, label, help, ...rest }) => {
  const handleChange = (newValue) => {
    onChange({ target: { id, value: newValue } }, { dirty: true });
  };

  return (
    <FormGroup controlId={id}>
      {label && <ControlLabel>{label}</ControlLabel>}
      <Select {...rest}
              id={id}
              matchProp="label"
              onChange={handleChange}
              components={{ Placeholder }}
              styles={{
                placeholder: base => ({
                  ...base,
                  fontSize: '14px',
                  color: '#999',
                  fontWeight: 400,
                  fontFamily: '"Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif',
                }),
              }} />
      {help && <HelpBlock>{help}</HelpBlock>}
    </FormGroup>
  );
};

ValidatedSelect.propTypes = {
  id: PropTypes.string.isRequired,
  onChange: PropTypes.func,
  label: PropTypes.string,
  help: PropTypes.string,
};

ValidatedSelect.defaultProps = {
  onChange: () => {},
  label: null,
  help: null,
};

export default ValidatedSelect;
