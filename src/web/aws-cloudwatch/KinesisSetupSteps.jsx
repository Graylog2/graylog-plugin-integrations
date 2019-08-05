import React from 'react';
import KinesisSetupStep from "./KinesisSetupStep";

const KinesisSetupSteps = ({}) => {

  return (
    <>
      <KinesisSetupStep label={"Step 1: Create Stream"} inProgress={false} success={true}/>
      <KinesisSetupStep label={"Step 2: Create Policy"} inProgress={false} success={true}/>
      <KinesisSetupStep label={"Step 3: Create Subscription"} inProgress={true} success={false}/>
    </>
  );
};

export default KinesisSetupSteps;
