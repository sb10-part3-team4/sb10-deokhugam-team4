package com.codeit.team4.deokhugam.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NaverBookProperties.class)   // 이 클래스를 활성화 함
public class NaverConfig {
}