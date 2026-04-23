package com.codeit.team4.deokhugam;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import com.codeit.team4.deokhugam.s3.S3Service;
import com.codeit.team4.deokhugam.s3.S3ServiceStub;

@TestConfiguration
public class TestConfig {

    @Bean
    @Profile("test")
    public S3Service s3Service() {
        return new S3ServiceStub();
    }
}