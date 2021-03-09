// eslint-disable-next-line react/prefer-stateless-function
import React from 'react';
import PropTypes from 'prop-types';

class GreyNoiseAdapterSummary extends React.Component {
  static propTypes = {
    dataAdapter: PropTypes.shape({
      config: PropTypes.shape({
      }).isRequired,
      updateConfig: PropTypes.func.isRequired,
      handleFormEvent: PropTypes.func.isRequired,
      validationState: PropTypes.func.isRequired,
      validationMessage: PropTypes.func.isRequired,
    }),
  };

  render() {
    const { config } = this.props.dataAdapter;

    return (
      <dl>
        <dt>API Token</dt>
        <dd>******</dd>
      </dl>
    );
  }
}

export default GreyNoiseAdapterSummary;
