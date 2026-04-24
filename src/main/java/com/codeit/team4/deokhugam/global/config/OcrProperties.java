package com.codeit.team4.deokhugam.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "ocr.api")
public class OcrProperties {
    private final String key;
    private final String url;

    public OcrProperties(String key, String url) {
        this.key = key;
        this.url = url;
    }
}