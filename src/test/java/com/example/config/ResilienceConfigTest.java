package com.example.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ResilienceConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ResilienceConfig.class);

    @Test
    void shouldCreateCircuitBreakerConfigWithCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "resilience4j.circuitbreaker.sliding-window-size=20",
                        "resilience4j.circuitbreaker.failure-rate-threshold=60.0",
                        "resilience4j.circuitbreaker.wait-duration-in-open-state=15",
                        "resilience4j.circuitbreaker.permitted-number-of-calls-in-half-open-state=5"
                )
                .run(context -> {
                    CircuitBreakerConfig circuitBreakerConfig = context.getBean(CircuitBreakerConfig.class);
                    assertThat(circuitBreakerConfig).isNotNull();
                    assertThat(circuitBreakerConfig.getSlidingWindowSize()).isEqualTo(20);
                    assertThat(circuitBreakerConfig.getFailureRateThreshold()).isEqualTo(60.0f);
                    IntervalFunction intervalFunction = circuitBreakerConfig.getWaitIntervalFunctionInOpenState();
                    long waitDurationInMillis = intervalFunction.apply(1);
                    assertThat(waitDurationInMillis).isEqualTo(Duration.ofSeconds(15).toMillis());
                    assertThat(circuitBreakerConfig.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(5);
                });
    }

    @Test
    void shouldUseDefaultValuesWhenPropertiesNotProvided() {
        contextRunner
                .run(context -> {
                    CircuitBreakerConfig circuitBreakerConfig = context.getBean(CircuitBreakerConfig.class);
                    assertThat(circuitBreakerConfig).isNotNull();
                    assertThat(circuitBreakerConfig.getSlidingWindowSize()).isEqualTo(10);
                    assertThat(circuitBreakerConfig.getFailureRateThreshold()).isEqualTo(50.0f);
                    IntervalFunction intervalFunction = circuitBreakerConfig.getWaitIntervalFunctionInOpenState();
                    long waitDurationInMillis = intervalFunction.apply(1);
                    assertThat(waitDurationInMillis).isEqualTo(Duration.ofSeconds(10).toMillis());
                    assertThat(circuitBreakerConfig.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
                });
    }
}