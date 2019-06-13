<<<<<<< HEAD
import React from 'react';
import PropTypes from 'prop-types';
=======
import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Row, Col } from 'react-bootstrap';
>>>>>>> Cleanup

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
  getValue: PropTypes.func.isRequired,
};

StepKinesis.defaultProps = {
  hasStreams: false,
};

<<<<<<< HEAD
export default StepKinesis;
=======
    return (
      <Row>
        <Col md="8">
          { hasStreams
              ? <CloudWatchStreams {...restProps} />
              : <CloudWatchKinesisSetup {...restProps} />
          }
        </Col>
      </Row>
    )
  }
}
>>>>>>> Cleanup
