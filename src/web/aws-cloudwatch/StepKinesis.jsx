import React from 'react';
import PropTypes from 'prop-types';

import KinesisStreams from './KinesisStreams';
import KinesisSetup from './KinesisSetup';
import { AdvancedOptionsProvider } from './providers/AdvancedOptions';

const StepKinesis = ({ hasStreams, ...restProps }) => {
  return (
    <AdvancedOptionsProvider>
      <React.Fragment>
        { hasStreams
          ? <KinesisStreams {...restProps} />
          : <KinesisSetup {...restProps} />
        }
      </React.Fragment>
    </AdvancedOptionsProvider>
  );
};

StepKinesis.propTypes = {
  hasStreams: PropTypes.bool,
  onChange: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
};

StepKinesis.defaultProps = {
  hasStreams: false,
};

export default StepKinesis;
