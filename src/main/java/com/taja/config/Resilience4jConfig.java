package com.taja.config;

import com.taja.global.exception.ApiException;
import com.taja.global.exception.NoRetryApiException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Configuration
public class Resilience4jConfig {

    public static final String WEATHER_API_RESILIENCE = "weatherApi";

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(100)  // 사실상 모든 실패에 대해 fallback 호출 (Circuit Breaker는 거의 열리지 않음)
                .waitDurationInOpenState(Duration.ofMillis(10000))
                .permittedNumberOfCallsInHalfOpenState(2)
                .slidingWindowSize(100)  // 매우 큰 값으로 설정하여 Circuit Breaker가 거의 열리지 않도록
                .recordExceptions(
                        IOException.class,
                        TimeoutException.class,
                        HttpServerErrorException.class,
                        HttpClientErrorException.class,
                        ApiException.class,
                        FeignException.class,
                        ResourceAccessException.class)
                .build();
    }

    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryOnException(throwable -> {
                    if (throwable instanceof NoRetryApiException) {
                        return false;
                    }
                    
                    if (throwable instanceof ApiException) {
                        return true;
                    }
                    
                    if (throwable instanceof FeignException) {
                        return true;
                    }
                    
                    if (throwable instanceof IOException) {
                        return true;
                    }
                    
                    if (throwable instanceof TimeoutException || throwable instanceof ResourceAccessException) {
                        return true;
                    }
                    
                    Throwable cause = throwable.getCause();
                    return cause instanceof FeignException;
                })
                .ignoreExceptions(NoRetryApiException.class)
                .build();
    }
}
