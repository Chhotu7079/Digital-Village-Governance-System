package com.dvgs.notification.config.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class SesConfig {

    @Bean
    public SesClient sesClient(@Value("${AWS_ACCESS_KEY:dummy}") String accessKey,
                               @Value("${AWS_SECRET_KEY:dummy}") String secretKey,
                               @Value("${AWS_REGION:ap-south-1}") String region) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return SesClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .build();
    }
}
