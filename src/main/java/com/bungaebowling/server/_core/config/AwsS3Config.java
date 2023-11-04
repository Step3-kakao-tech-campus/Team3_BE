package com.bungaebowling.server._core.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AwsS3Config {
    @Value("s3-ap-northeast-2.amazonaws.com")
    private String endpoint;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("krmp-proxy.9rum.cc")
    private String proxyHost;

    @Value("3128")
    private int proxyPort;

    @Bean
    @Profile({"local", "prod", "test"})
    public AmazonS3 amazonS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    @Bean
    @Profile("deploy")
    public AmazonS3 amazonS3ClientForDeploy() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(60000);  // 연결 타임아웃 시간 60000ms = 60s 설정
        clientConfiguration.setSocketTimeout(60000);  // 소켓 타임아웃 시간 60000ms = 60s 설정
        clientConfiguration.setProxyHost(proxyHost);
        clientConfiguration.setProxyPort(proxyPort);
        //clientConfiguration.setSignerOverride("S3SignerType");

        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }
}
