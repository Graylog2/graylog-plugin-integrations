import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';

import formValidation from '../utils/formValidation';

export default class FormWrap extends Component {
  static propTypes = {
    children: PropTypes.any.isRequired,
    onSubmit: PropTypes.func,
    buttonContent: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.node,
    ]),
  }

  static defaultProps = {
    onSubmit: () => {},
    buttonContent: 'Submit',
  }

  onSubmit = (event) => {
    const { onSubmit } = this.props;
    if (formValidation.isFormValid(this.currentForm)) {
      onSubmit(event);
    }
  };

  prevent = (event) => {
    event.preventDefault();
    return false;
  }

  render() {
    const { children, buttonContent } = this.props;

    return (
      <form onSubmit={this.prevent} autoComplete="off" ref={(form) => { this.currentForm = form; }}>
        {children}

        <Button type="button"
                onClick={this.onSubmit}
                bsStyle="primary">
          {buttonContent}
        </Button>
      </form>
    );
  }
}
