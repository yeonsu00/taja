package com.taja.bikeapi.application;

import com.taja.global.exception.ApiException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class BikeApiBatchFetcher {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_SECONDS = 1;

    private static final Set<String> NON_RETRYABLE_ERROR_CODES = Set.of(
            "ERROR-300", "ERROR-301", "ERROR-310", "ERROR-331",
            "ERROR-332", "ERROR-333", "ERROR-334", "ERROR-335", "ERROR-336", "INFO-200"
    );

    public <T> Mono<List<T>> fetchAll(int totalCount, int batchSize, BatchFetcher<T> fetcher) {
        int pageCount = (int) Math.ceil((double) totalCount / batchSize);

        return Flux.range(0, pageCount)
                .flatMap(page -> {
                    int startIndex = 1 + (page * batchSize);
                    int endIndex = startIndex + batchSize - 1;

                    return fetcher.fetch(startIndex, endIndex)
                            .doOnSuccess(data -> log.info("✅ API 요청 성공 ({}-{}) | 수집된 데이터 수: {}",
                                    startIndex, endIndex, data.size()))
                            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(RETRY_DELAY_SECONDS))
                                    .filter(this::isRetryableError)
                                    .doBeforeRetry(retrySignal -> log.warn("API 요청 재시도... ({}-{}) | 시도 횟수: {} | 원인: {}",
                                            startIndex, endIndex, retrySignal.totalRetries() + 1,
                                            retrySignal.failure().getMessage()))
                            )
                            .onErrorResume(error -> {
                                log.error("API 요청 최종 실패 ({}-{}), 원인: {} | 코드: {}",
                                        startIndex, endIndex, getErrorMessage(error), getErrorCode(error));
                                return Mono.just(List.of());
                            });
                })
                .flatMap(Flux::fromIterable)
                .collectList();
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof ApiException apiException) {
            return !NON_RETRYABLE_ERROR_CODES.contains(apiException.getCode());
        }
        return true;
    }

    private String getErrorCode(Throwable error) {
        return (error instanceof ApiException)
                ? ((ApiException) error).getCode() : "UNKNOWN";
    }

    private String getErrorMessage(Throwable error) {
        return error.getMessage().replace("\r\n", " ").replace("\n", " ").trim();
    }

}
