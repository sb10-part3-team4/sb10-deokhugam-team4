package com.codeit.team4.deokhugam.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Profile("prod")
public class S3ProdConfig {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of("ap-northeast-2"))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}