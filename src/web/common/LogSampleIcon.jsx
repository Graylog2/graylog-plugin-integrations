import React from 'react';
import PropTypes from 'prop-types';
import styled from '@emotion/styled';

const LogSampleIcon = ({ className, success }) => {
  return (
    <IconWrap success={success} className={className}>
      <Icon className="fa fa-smile-o" />
    </IconWrap>
  );
};

LogSampleIcon.propTypes = {
  success: PropTypes.bool,
  className: PropTypes.string,
};

LogSampleIcon.defaultProps = {
  success: false,
  className: null,
};

const IconWrap = styled.span`
  color: ${props => (props.success ? '#00AE42' : '#AD0707')};
`;

const Icon = styled.i`
  font-size: 21px;
`;

export default LogSampleIcon;
