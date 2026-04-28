package com.codeit.team4.deokhugam.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisConnectionProperties {
    private final String host;
    private final int port;

    public RedisConnectionProperties(String host, int port) {
        this.host = host;
        this.port = port;
    }
}