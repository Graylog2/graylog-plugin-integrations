import React, { useContext } from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';

import { Input } from 'components/bootstrap';
import { ExternalLink } from 'components/common';

import { FormDataContext } from 'aws/context/FormData';
import { AdvancedOptionsContext } from 'aws/context/AdvancedOptions';
import AdditionalFields from 'aws/common/AdditionalFields';

const StyledAdditionalFields = styled(AdditionalFields)`
  margin: 0 0 35px;
`;

const Title = styled.h3`
  margin: 18px 0 3px;
`;

const SubTitle = styled.h4`
  font-style: italic;
  margin: 0 0 12px;
`;

const AWSEndpointAndARN = ({ onChange }) => {
  const { formData } = useContext(FormDataContext);
  const { isAWSEndpointAndARNVisible, setAWSEndpointAndARNVisibility } = useContext(AdvancedOptionsContext);

  const {
    awsEndpointCloudWatch = { value: '' },
    awsEndpointDynamoDB = { value: '' },
    awsEndpointIAM = { value: '' },
    awsEndpointKinesis = { value: '' },
  } = formData;

  const handleToggle = (visible) => {
    setAWSEndpointAndARNVisibility(visible);
  };

  return (
    <StyledAdditionalFields title="Optional AWS VPC Endpoints"
                            visible={isAWSEndpointAndARNVisible}
                            onToggle={handleToggle}>


      <Title>Overrides the default AWS API endpoint URL that Graylog communicates with.</Title>
      <SubTitle>Use this is you are using <ExternalLink href="https://docs.aws.amazon.com/vpc/latest/userguide/vpc-endpoints.html">VPC Endpoints</ExternalLink> for AWS services.</SubTitle>

      <Input id="awsEndpointCloudWatch"
             type="text"
             value={awsEndpointCloudWatch.value}
             onChange={onChange}
             label="CloudWatch API Endpoint Override" />

      <Input id="awsEndpointIAM"
             type="text"
             value={awsEndpointIAM.value}
             onChange={onChange}
             label="IAM API Endpoint Override" />

      <Input id="awsEndpointDynamoDB"
             type="text"
             value={awsEndpointDynamoDB.value}
             onChange={onChange}
             label="DynamoDB API Endpoint Override" />

      <Input id="awsEndpointKinesis"
             type="text"
             value={awsEndpointKinesis.value}
             onChange={onChange}
             label="Kinesis API Endpoint Override" />
    </StyledAdditionalFields>
  );
};

AWSEndpointAndARN.propTypes = {
  onChange: PropTypes.func.isRequired,
};

export default AWSEndpointAndARN;
