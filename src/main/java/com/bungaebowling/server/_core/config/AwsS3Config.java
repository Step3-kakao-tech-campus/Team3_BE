package com.bungaebowling.server._core.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class AwsS3Config {
    @Value("${cloud.aws.s3.endpoint}")
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

        System.out.println("region: "+ region);
        log.info("region: "+region);

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

        System.out.println("deploy region: "+ region);
        //System.out.println("endpoint: "+ endpoint);

        log.info("deploy region: "+ region);
        log.info("access key: "+ accessKey);
        log.info("proxyHost: "+ proxyHost);
        log.info("proxyPort: "+ proxyPort);
        log.info("endpoint: "+ endpoint);

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(60000);  // 연결 타임아웃 시간 60000ms = 60s 설정
        clientConfiguration.setSocketTimeout(60000);  // 소켓 타임아웃 시간 60000ms = 60s 설정
        clientConfiguration.setProxyHost(proxyHost);
        clientConfiguration.setProxyPort(proxyPort);
        //clientConfiguration.setSignerOverride("S3SignerType");

        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, null);
        //System.out.println("endpoint config: "+ endpointConfiguration);
        log.info("endpoint config: "+ endpointConfiguration);

        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                //.withRegion(region)
                .build();
    }
}
