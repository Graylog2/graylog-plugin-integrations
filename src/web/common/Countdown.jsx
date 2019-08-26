import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { duration as momentDuration } from 'moment';

function Countdown({ callback, className, timeInSeconds, paused }) {
  const [currentTime, setCurrentTime] = useState('00:00');
  let logInterval;
  let tickTock = timeInSeconds;

  const startCountdown = () => {
    logInterval = setInterval(() => {
      const duration = momentDuration(tickTock, 'seconds');

      tickTock -= 1;

      if (duration < 0) {
        tickTock = timeInSeconds;
        setCurrentTime('00:00');
        clearInterval(logInterval);
        callback();
      } else {
        setCurrentTime(duration.format('mm:ss'));
      }
    }, 1000);
  };

  useEffect(() => {
    if (paused) {
      clearInterval(logInterval);
    } else {
      startCountdown();
    }

    return () => {
      clearInterval(logInterval);
    };
  }, [paused]);

  return (
    <span className={className}>{currentTime}</span>
  );
}

Countdown.propTypes = {
  timeInSeconds: PropTypes.number.isRequired,
  callback: PropTypes.func,
  className: PropTypes.string,
  paused: PropTypes.bool,
};

Countdown.defaultProps = {
  callback: () => {},
  className: '',
  paused: false,
};

export default Countdown;
