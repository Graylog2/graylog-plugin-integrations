import React from 'react';
import PropTypes from 'prop-types';

import KinesisStreams from './KinesisStreams';
import KinesisSetup from './KinesisSetup';

const StepKinesis = ({ hasStreams, ...restProps }) => {
  return (
    <React.Fragment>
      { hasStreams
        ? <KinesisStreams {...restProps} />
        : <KinesisSetup {...restProps} />
      }
    </React.Fragment>
  );
};

StepKinesis.propTypes = {
  hasStreams: PropTypes.bool,
  onSubmit: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  toggleAdvancedOptions: PropTypes.func,
};

StepKinesis.defaultProps = {
  hasStreams: false,
  toggleAdvancedOptions: () => {},
};

export default StepKinesis;
