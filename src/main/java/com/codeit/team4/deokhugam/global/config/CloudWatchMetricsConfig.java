package com.codeit.team4.deokhugam.global.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClientBuilder;

@Configuration
@ConditionalOnProperty(name = "management.cloudwatch.metrics.export.enabled", havingValue = "true")
public class CloudWatchMetricsConfig {

    private static final String CLOUDWATCH_EXPORT_PREFIX = "management.cloudwatch.metrics.export";

    @Bean
    public CloudWatchMeterRegistry cloudWatchMeterRegistry(
            CloudWatchConfig cloudWatchConfig,
            Clock clock,
            CloudWatchAsyncClient cloudWatchAsyncClient
    ) {
        return new CloudWatchMeterRegistry(cloudWatchConfig, clock, cloudWatchAsyncClient);
    }

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient(Environment env) {
        String region = env.getProperty("aws.s3.region", env.getProperty("AWS_REGION"));

        CloudWatchAsyncClientBuilder builder = CloudWatchAsyncClient.builder();
        if (region != null && !region.isBlank()) {
            builder.region(Region.of(region));
        }
        return builder.build();
    }

    @Bean
    public CloudWatchConfig cloudWatchConfig(Environment env) {
        Binder binder = Binder.get(env);

        return new CloudWatchConfig() {
            @Override
            public String get(String key) {
                return switch (key) {
                    case "cloudwatch.namespace" -> env.getProperty(
                            CLOUDWATCH_EXPORT_PREFIX + ".namespace",
                            "deokhugam"
                    );
                    case "cloudwatch.step" -> binder.bind(
                                    CLOUDWATCH_EXPORT_PREFIX + ".step",
                                    Duration.class
                            )
                            .orElse(Duration.ofMinutes(1))
                            .toString();
                    case "cloudwatch.enabled" -> env.getProperty(
                            CLOUDWATCH_EXPORT_PREFIX + ".enabled",
                            "true"
                    );
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
