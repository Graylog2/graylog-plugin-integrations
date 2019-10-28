package org.graylog.integrations.aws;

import com.sun.istack.internal.Nullable;
import org.graylog.integrations.aws.transports.AWSPluginConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;

import static com.google.common.base.Strings.isNullOrEmpty;


public class AWSAuthProvider implements AwsCredentialsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AWSAuthProvider.class);

    private AwsCredentialsProvider credentials;

    public AWSAuthProvider(AWSPluginConfiguration config) {
        this(config, null, null, null, null);
    }

    public AWSAuthProvider(AWSPluginConfiguration config,
                           @Nullable String accessKey,
                           @Nullable String secretKey,
                           @Nullable String region,
                           @Nullable String assumeRoleArn) {
        this.credentials = this.resolveAuthentication(config, accessKey, secretKey, region, assumeRoleArn);
    }

    private AwsCredentialsProvider resolveAuthentication(AWSPluginConfiguration config,
                                                         @Nullable String accessKey,
                                                         @Nullable String secretKey,
                                                         @Nullable String region,
                                                         @Nullable String assumeRoleArn) {
        AwsCredentialsProvider awsCredentials;
        if (!isNullOrEmpty(accessKey) && !isNullOrEmpty(secretKey)) {
            awsCredentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
            LOG.debug("Using input specific config");
        } else if (!isNullOrEmpty(config.accessKey()) && !isNullOrEmpty(config.secretKey())) {
            awsCredentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(config.accessKey(), config.secretKey()));
            LOG.debug("Using AWS Plugin config");
        } else {
            awsCredentials = DefaultCredentialsProvider.create();
            LOG.debug("Using Default Provider Chain");
        }
        if (!isNullOrEmpty(assumeRoleArn) && !isNullOrEmpty(region)) {
            LOG.debug("Creating cross account assume role credentials");
            return this.getSTSCredentialsProvider(awsCredentials, region, assumeRoleArn);
        } else {
            return awsCredentials;
        }
    }

    private AwsCredentialsProvider getSTSCredentialsProvider(AwsCredentialsProvider awsCredentials, String region, String assumeRoleArn) {

        StsClient stsClient = StsClient.builder().region(Region.of(region)).credentialsProvider(awsCredentials).build();
        String roleSessionName = String.format("API_KEY_%s@ACCOUNT_%s",
                                               awsCredentials.resolveCredentials().accessKeyId(),
                                               stsClient.getCallerIdentity(GetCallerIdentityRequest.builder().build()).account());
        LOG.debug("Cross account role session name: " + roleSessionName);
        return StsAssumeRoleCredentialsProvider.builder().refreshRequest(AssumeRoleRequest.builder()
                                                                                          .roleSessionName(roleSessionName)
                                                                                          .roleArn(assumeRoleArn)
                                                                                          .build())
                                               .stsClient(stsClient)
                                               .build();
    }

    @Override
    public AwsCredentials resolveCredentials() {
        return null;
    }
}
