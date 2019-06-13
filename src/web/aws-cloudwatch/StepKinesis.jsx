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
  values: PropTypes.object.isRequired,
  toggleAdvancedOptions: PropTypes.func,
  visibleAdvancedOptions: PropTypes.bool,
};

StepKinesis.defaultProps = {
  hasStreams: false,
  visibleAdvancedOptions: false,
  toggleAdvancedOptions: () => {},
};

export default StepKinesis;
