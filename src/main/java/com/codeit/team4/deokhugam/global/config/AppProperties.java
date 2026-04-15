package com.codeit.team4.deokhugam.global.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(List<String> clientIpHeaders) {
}
