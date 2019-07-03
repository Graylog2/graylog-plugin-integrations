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
  isAdvancedOptionsVisible: PropTypes.bool,
  onChange: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  setAdvancedOptionsVisiblity: PropTypes.func,
};

StepKinesis.defaultProps = {
  hasStreams: false,
  isAdvancedOptionsVisible: false,
  setAdvancedOptionsVisiblity: () => {},
};

export default StepKinesis;
