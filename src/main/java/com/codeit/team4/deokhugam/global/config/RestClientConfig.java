package com.codeit.team4.deokhugam.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 타임아웃 설정
        factory.setConnectTimeout(3000); // 3초
        factory.setReadTimeout(5000);    // 5초

        return builder
                .requestFactory(factory)
                .build();
    }
}