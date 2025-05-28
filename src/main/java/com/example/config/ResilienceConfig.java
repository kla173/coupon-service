package com.example.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Value("${resilience4j.circuitbreaker.sliding-window-size:10}")
    private int slidingWindowSize;

    @Value("${resilience4j.circuitbreaker.failure-rate-threshold:50}")
    private float failureRateThreshold;

    @Value("${resilience4j.circuitbreaker.wait-duration-in-open-state:10}")
    private long waitDurationInOpenState;

    @Value("${resilience4j.circuitbreaker.permitted-number-of-calls-in-half-open-state:3}")
    private int permittedNumberOfCallsInHalfOpenState;

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(slidingWindowSize)
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofSeconds(waitDurationInOpenState))
                .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                .build();
    }
}