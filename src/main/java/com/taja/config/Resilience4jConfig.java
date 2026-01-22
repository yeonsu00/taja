package com.taja.config;

import com.taja.global.exception.ApiException;
import com.taja.global.exception.NoRetryApiException;
import feign.FeignException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Configuration
public class Resilience4jConfig {

    public static final String WEATHER_API_RESILIENCE = "weatherApi";

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
                    if (cause instanceof FeignException) {
                        return true;
                    }
                    if (cause instanceof IOException) {
                        return true;
                    }
                    return cause instanceof TimeoutException || cause instanceof ResourceAccessException;
                })
                .ignoreExceptions(NoRetryApiException.class)
                .build();
    }

    @Bean
    public Retry weatherApiRetry(RetryRegistry retryRegistry, RetryConfig retryConfig) {
        return retryRegistry.retry(WEATHER_API_RESILIENCE, retryConfig);
    }
}
