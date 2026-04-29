package com.codeit.team4.deokhugam.global.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

@Configuration
@Profile("prod")
public class CloudWatchMetricsConfig {

    @Bean
    public CloudWatchMeterRegistry cloudWatchMeterRegistry(
            CloudWatchConfig cloudWatchConfig,
            Clock clock,
            CloudWatchAsyncClient cloudWatchAsyncClient
    ) {
        return new CloudWatchMeterRegistry(cloudWatchConfig, clock, cloudWatchAsyncClient);
    }

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.create();
    }

    @Bean
    public CloudWatchConfig cloudWatchConfig(Environment env) {
        return new CloudWatchConfig() {
            @Override
            public String get(String key) {
                return switch (key) {
                    case "cloudwatch.namespace" ->
                            env.getProperty("management.cloudwatch.metrics.export.namespace", "deokhugam");
                    case "cloudwatch.step" ->
                            env.getProperty("management.cloudwatch.metrics.export.step", "PT1M");
                    default -> null;
                };
            }
        };
    }

    @Bean
    public Clock micrometerClock() {
        return Clock.SYSTEM;
    }
}
