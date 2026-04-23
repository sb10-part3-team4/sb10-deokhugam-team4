package com.codeit.team4.deokhugam.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {

    private final String bucketName;
    private final String region;
    private final String accessKey;
    private final String secretKey;

    public S3Properties(String bucketName, String region, String accessKey, String secretKey) {
        this.bucketName = bucketName;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }
}
