import React, { Component } from 'react'
import PropTypes from 'prop-types'

import CloudWatchStreams from './KinesisStreams'
import CloudWatchKinesisSetup from './KinesisSetup'

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
            ? <CloudWatchStreams {...restProps} />
            : <CloudWatchKinesisSetup {...restProps} />
        }
      </React.Fragment>
    )
  }
}
