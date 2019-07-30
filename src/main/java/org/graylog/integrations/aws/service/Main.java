package org.graylog.integrations.aws.service;

public class Main {
    public static void main(String[] args) {

        String roleArn;

        roleArn = KinesisService.autoKinesisPermissionsRequired("us-east-1", "", "", "claudia-kinesis-graylog-stream", "rolePermissionsTest-roleName1", "rolePermissionsTest-rolePolicyName");
        System.out.println("Role Arn is: " + roleArn);


    }


}
