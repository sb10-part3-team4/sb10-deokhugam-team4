package com.codeit.team4.deokhugam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DeokhugamApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeokhugamApplication.class, args);
    }

}
