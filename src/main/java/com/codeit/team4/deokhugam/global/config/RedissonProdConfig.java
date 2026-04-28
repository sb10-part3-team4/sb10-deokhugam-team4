package com.codeit.team4.deokhugam.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
@EnableConfigurationProperties(RedisConnectionProperties.class)
public class RedissonProdConfig {
    @Bean
    public RedissonClient redissonClient(RedisConnectionProperties redisProps) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisProps.getHost() + ":" + redisProps.getPort());
        return Redisson.create(config);
    }
}