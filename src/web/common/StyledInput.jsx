import React, { Component } from 'react';
import PropTypes from 'prop-types';
import styled from '@emotion/styled';

import { Input } from 'components/bootstrap';
import formValidation from '../utils/formValidation';

export default class StyledInput extends Component {
  static propTypes = {
    onChange: PropTypes.func,
    errorMessage: PropTypes.string,
  }

  static defaultProps = {
    onChange: () => {},
    errorMessage: undefined,
  }

  state = {
    invalid: false,
    errorOutput: undefined,
  }

  checkValidity = (event) => {
    /* TODO:
      - extract this into a helper function
      - Accept the node element, return boolean
      - http://alistapart.com/article/forward-thinking-form-validation/#section6
      - Pass or set bsStyle={'success' || 'warning' || 'error' || null}
      - Maybe pass or set `invalid` as prop
    */

    const { errorMessage, onChange } = this.props;
    const errorOutput = formValidation.checkInputValidity(event.target, errorMessage);

    this.setState({
      invalid: !!errorOutput,
      errorOutput: errorOutput,
    });

    onChange(event);
  };

  render() {
    const { errorOutput, invalid } = this.state;

    return (
      <React.Fragment>
        <AnInputThatIsStyled {...this.props}
                             onChange={this.checkValidity}
                             invalid={invalid}
                             help={invalid && <RequiredText>{errorOutput}</RequiredText>} />

      </React.Fragment>
    );
  }
}

// Remove unnecessary props before generating component
const AnInputThatIsStyled = styled(({ invalid, errorMessage, ...rest }) => <Input {...rest} />)`
  border-color: ${props => (props.invalid ? '#AD0707' : 'inherit')};
`;

const RequiredText = styled.span`
  color: rgba(173, 7, 7, 0.7);
`;
