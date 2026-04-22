package com.codeit.team4.deokhugam;

import com.codeit.team4.deokhugam.naver.NaverBookProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(NaverBookProperties.class)
public class DeokhugamApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeokhugamApplication.class, args);
    }

}
