import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';
import styled from '@emotion/styled';

export default class StyledForm extends Component {
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

    if (this.currentForm.checkValidity()) {
      onSubmit(event);
    } else {
      onSubmit(false, this.currentForm);
    }
  };

  prevent = (event) => {
    event.preventDefault();
    return false;
  }

  render() {
    const { children, buttonContent } = this.props;

    return (
      <AFormThatIsStyled onSubmit={this.prevent} autoComplete="off" ref={(form) => { this.currentForm = form; }}>
        {children}

        <Button type="button"
                onClick={this.onSubmit}
                bsStyle="primary">
          {buttonContent}
        </Button>
      </AFormThatIsStyled>
    );
  }
}

const AFormThatIsStyled = styled.form`
  width: 550px;
  margin: 0 auto;
`;
