package org.graylog.integrations.aws.transports;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;

public class AWSAuthProvider implements AWSCredentialsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AWSAuthProvider.class);

    private AWSCredentialsProvider credentials;

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

    private AWSCredentialsProvider resolveAuthentication(AWSPluginConfiguration config,
                                                         @Nullable String accessKey,
                                                         @Nullable String secretKey,
                                                         @Nullable String region,
                                                         @Nullable String assumeRoleArn) {
        AWSCredentialsProvider awsCredentials;
        if (!isNullOrEmpty(accessKey) && !isNullOrEmpty(secretKey)) {
            awsCredentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
            LOG.debug("Using input specific config");
        } else if (!isNullOrEmpty(config.accessKey()) && !isNullOrEmpty(config.secretKey())) {
            awsCredentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(config.accessKey(), config.secretKey()));
            LOG.debug("Using AWS Plugin config");
        } else {
            awsCredentials = new DefaultAWSCredentialsProviderChain();
            LOG.debug("Using Default Provider Chain");
        }

        // TODO: Add assume role support here.

        return awsCredentials;
    }

    @Override
    public AWSCredentials getCredentials() {
        return this.credentials.getCredentials();
    }

    @Override
    public void refresh() {
        this.credentials.refresh();
    }
}
