package com.codeit.team4.deokhugam.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "naver.api")
public class NaverBookProperties {
    private final String clientId;
    private final String clientSecret;
    private final String bookSearchUrl;

    public NaverBookProperties(String clientId, String clientSecret, String bookSearchUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.bookSearchUrl = bookSearchUrl;
    }

    @Override
    public String toString() {
        return "NaverBookProperties{" +
                "clientId='" + clientId + '\'' +
                ", clientSecret='***'" +
                ", bookSearchUrl='" + bookSearchUrl + '\'' +
                '}';
    }
}