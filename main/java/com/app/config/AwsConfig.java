package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.region.static:us-east-1}")
    private String region;

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder().region(Region.of(region)).build();
    }
}
