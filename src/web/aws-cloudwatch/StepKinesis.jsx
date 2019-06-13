import React, { Component } from 'react'
import PropTypes from 'prop-types'

import KinesisStreams from './KinesisStreams'
import KinesisSetup from './KinesisSetup'

export default class StepKinesis extends Component {
  static propTypes = {
    hasStreams: PropTypes.bool,
    onSubmit: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired,
    getValue: PropTypes.func.isRequired,
  }

  static defaultProps = {
    hasStreams: false,
  }

  render() {
    const { hasStreams, ...restProps } = this.props;

    return (
      <React.Fragment>
        { hasStreams
            ? <KinesisStreams {...restProps} />
            : <KinesisSetup {...restProps} />
        }
      </React.Fragment>
    )
  }
}
