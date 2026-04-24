package com.codeit.team4.deokhugam.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@EnableConfigurationProperties(OcrProperties.class)
public class OcrConfig {
}