package org.graylog.integrations.aws;

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

import javax.annotation.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;


/**
 * Resolves the appropriate AWS authorization provider.
 *
 * If an {@code accessKey} and {@code secretKey} are provided, they will be used explicitly.
 * If not, the default DefaultCredentialsProvider will be used instead. This will resolve the policy to
 * use using Java props, environment variables, EC2 instance roles etc. See the {@link DefaultCredentialsProvider}
 * Javadoc for more information.
 */
public class AWSAuthProvider implements AwsCredentialsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AWSAuthProvider.class);

    private AwsCredentials credentials;

    public AWSAuthProvider(@Nullable String accessKey,
                           @Nullable String secretKey,
                           @Nullable String region,
                           @Nullable String assumeRoleArn) {
        this.credentials = this.resolveAuthentication(accessKey, secretKey, region, assumeRoleArn);
    }

    private AwsCredentials resolveAuthentication(@Nullable String accessKey,
                                                 @Nullable String secretKey,
                                                 @Nullable String region,
                                                 @Nullable String assumeRoleArn) {
        AwsCredentialsProvider awsCredentials;
        if (!isNullOrEmpty(accessKey) && !isNullOrEmpty(secretKey)) {
            awsCredentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
            LOG.debug("Using explicitly provided key and secret.");
        } else {
            awsCredentials = DefaultCredentialsProvider.create();
            LOG.debug("Using default authorization provider chain.");
        }

        // Apply the Assume Role ARN Authorization if specified.
        if (!isNullOrEmpty(assumeRoleArn) && !isNullOrEmpty(region)) {
            LOG.debug("Creating cross account assume role credentials");
            return applyStsCredentialsProvider(awsCredentials, region, assumeRoleArn, accessKey)
                    .resolveCredentials();
        }

        return awsCredentials.resolveCredentials();
    }

    /**
     * In order to assume a role, a role must be provided to the AWS STS client a role that has the "sts:AssumeRole"
     * permission, which allows a role to be assumed.
     *
     * @param awsCredentials A pre-initialized AwsCredentialsProvider that has a role, which is authorized for the
     *                       "sts:AssumeRole" permission.
     * @param region         The desired AWS region.
     * @param assumeRoleArn  The ARN for the role to assume eg. arn:aws:iam::account-number:role/role-name
     * @param accessKey      The AWS access key if specified.
     * @return A new AwsCredentialsProvider instance which will assume the indicated role.
     */
    private AwsCredentialsProvider applyStsCredentialsProvider(AwsCredentialsProvider awsCredentials, String region,
                                                               String assumeRoleArn, @Nullable String accessKey) {

        StsClient stsClient = StsClient.builder().region(Region.of(region)).credentialsProvider(awsCredentials).build();

        // The custom roleSessionName is extra metadata, which will be logged in AWS CloudTrail with each request
        // to help with auditing and debugging.
        String roleSessionName = String.format("ACCESS_KEY_%s@ACCOUNT_%s",
                                               accessKey != null ? accessKey : "NONE", // Use "NONE" when access key not provided.
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
        return credentials;
    }
}
