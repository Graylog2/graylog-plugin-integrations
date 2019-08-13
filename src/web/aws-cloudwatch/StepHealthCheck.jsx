import React, { useContext, useEffect } from 'react';
import PropTypes from 'prop-types';
import { Panel } from 'react-bootstrap';
import styled from 'styled-components';

import { Input } from 'components/bootstrap';

import FormWrap from '../common/FormWrap';
import useFetch from '../common/hooks/useFetch';
import { ApiRoutes } from '../common/Routes';

import { ApiContext } from './context/Api';
import { FormDataContext } from './context/FormData';

const StepHealthCheck = ({ onSubmit }) => {
  const { logData, setLogData } = useContext(ApiContext);
  const { formData } = useContext(FormDataContext);

  const [, setLogDataUrl] = useFetch(
    null,
    (response) => {
      setLogData(response);
    },
    'POST',
    {
      region: formData.awsCloudWatchAwsRegion.value,
      stream_name: formData.awsCloudWatchKinesisStream.value,
    },
  );

  useEffect(() => {
    if (!logData) {
      setLogDataUrl(ApiRoutes.INTEGRATIONS.AWS.KINESIS.HEALTH_CHECK);
    }
  }, []);

  if (!logData) {
    return (
      <Panel bsStyle="warning"
             header={(
               <Notice><i className="fa fa-exclamation-triangle fa-2x" />
                 <span>We haven&apos;t received a response back from Amazon yet.</span>
               </Notice>
           )}>
        Hang out for a few moments while we keep checking your AWS stream for logs. Amazon&apos;s servers parse logs every 10 minutes, so grab a cup of coffee because this may take some time!

        Do not refresh your browser, this page will automatically refresh when your logs are available.
      </Panel>
    );
  }

  const unknownLog = logData.type === 'KINESIS_RAW';
  const iconClass = unknownLog ? 'times' : 'check';
  const acknowledgment = unknownLog ? 'Drats!' : 'Awesome!';
  const bsStyle = unknownLog ? 'warning' : 'success';

  let logType;

  switch (logData.type) {
    case 'KINESIS_FLOW_LOGS':
      logType = 'a Flow Log';
      break;

    default:
      logType = 'an unknown';
      break;
  }

  return (
    <FormWrap onSubmit={onSubmit}
              buttonContent="Review &amp; Finalize"
              disabled={false}
              title="Create Kinesis Stream"
              description={<p>We&apos;re going to attempt to parse a single log to help you out! If we&apos;re unable to, or you would like it parsed differently, head on over to <a href="/system/pipelines">Pipeline Rules</a> to set up your own parser!</p>}>

      <Panel bsStyle={bsStyle}
             header={(
               <Notice><i className={`fa fa-${iconClass} fa-2x`} />
                 <span>{acknowledgment} looks like <em>{logType}</em> log type.</span>
               </Notice>
                  )}>
        {unknownLog ? "Not to worry, we've parsed what we could and you can build Pipeline Rules to do the rest!" : "Take a look at what we've parsed so far and you can create Pipeline Rules to handle even more!"}
      </Panel>

      <Input id="awsCloudWatchLog"
             type="textarea"
             label="Formatted CloudWatch Log"
             value={logData.message}
             rows={10}
             disabled />
    </FormWrap>
  );
};

StepHealthCheck.propTypes = {
  onSubmit: PropTypes.func.isRequired,
};

const Notice = styled.span`
  display: flex;
  align-items: center;

  > span {
    margin-left: 6px;
  }
`;

export default StepHealthCheck;
