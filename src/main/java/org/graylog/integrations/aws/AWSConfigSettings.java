package org.graylog.integrations.aws;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;

public class AWSConfigSettings {

    public static AwsBasicCredentials createUser(){

        AwsBasicCredentials basicCredentials =
                AwsBasicCredentials.create(UserCredentials.getAccessKey(), UserCredentials.getSecretKey());

        return basicCredentials;
    }


    public static void setRegion(){

        int regionsAvailable = Region.regions().size();
        for (int i=0; i<regionsAvailable; i++){
            // List available regions
            String region = Region.regions().get(i).toString();
        }
        // Hardcode region to us-east-1
        Region.regions().get(15);
    }


}

