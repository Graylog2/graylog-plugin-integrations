import React, { useState, /* useEffect, */ useRef } from 'react';
// import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import { ControlLabel, FormGroup, HelpBlock } from 'react-bootstrap';

import { Select } from 'components/common';

const ValidatedSelect = ({ id, onChange, label, help, ...rest }) => {
  const selectRef = useRef();
  const [menuOpen, setMenuOpen] = useState(false);
  const handleChange = (newValue) => {
    onChange({ target: { id, value: newValue } }, { dirty: true });
  };

  // TODO: Track if the user clicks outside of the FormGroup and close the Select menu
  // const handleClick = (event) => {
  //   // ReactDOM.findDOMNode().contains(selectRef);
  //   console.log('findDOMNode', ReactDOM.findDOMNode(selectRef));
  // };

  // useEffect(() => {
  //   document.addEventListener('click', handleClick, false);
  //   return () => {
  //     document.removeEventListener('click', handleClick, false);
  //   };
  // }, []);

  return (
    <FormGroup controlId={id} ref={selectRef}>
      {label && <ControlLabel onClick={() => { setMenuOpen(!menuOpen); }}>{label}</ControlLabel>}
      <Select {...rest}
              id={id}
              matchProp="label"
              onChange={handleChange}
              menuIsOpen={menuOpen}
              onMenuOpen={() => { setMenuOpen(!menuOpen); }}
              onMenuClose={() => { setMenuOpen(false); }}
              onBlur={() => { setMenuOpen(false); }} />
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
