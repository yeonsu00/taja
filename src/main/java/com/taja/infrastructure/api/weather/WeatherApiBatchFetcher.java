package com.taja.infrastructure.api.weather;

import com.taja.global.exception.ApiException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherApiBatchFetcher {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_SECONDS = 1;

    private static final Set<String> NON_RETRYABLE_ERROR_CODES = Set.of(
            "03", // NODATA_ERROR (데이터 없음)
            "10", // INVALID_REQUEST_PARAMETER_ERROR (잘못된 요청 파라미터)
            "11", // NO_MANDATORY_REQUEST_PARAMETERS_ERROR (필수 파라미터 없음)
            "12", // NO_OPENAPI_SERVICE_ERROR (해당 서비스 없음)
            "20", // SERVICE_ACCESS_DENIED_ERROR (서비스 접근 거부)
            "21", // TEMPORARILY_DISABLE_THE_SERVICEKEY_ERROR (키 일시 중지)
            "30", // SERVICE_KEY_IS_NOT_REGISTERED_ERROR (등록되지 않은 키)
            "31", // DEADLINE_HAS_EXPIRED_ERROR (키 기한 만료)
            "32", // UNREGISTERED_IP_ERROR (등록되지 않은 IP)
            "33"  // UNSIGNED_CALL_ERROR (서명되지 않은 호출)
    );

    public <IN, OUT> Mono<List<OUT>> fetchAllConcurrently(List<IN> items,
                                                           Function<IN, Mono<OUT>> fetchFunction,
                                                           int maxConcurrentRequests,
                                                           Function<IN, String> itemLogKeyExtractor) {
        return Flux.fromIterable(items)
                .flatMap(item -> {
                            String logKey = itemLogKeyExtractor.apply(item);

                            return fetchFunction.apply(item)
                                    .doOnSuccess(data -> log.info("✅ 초단기실황 API 요청 성공 (Item: {})", logKey))
                                    .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(RETRY_DELAY_SECONDS))
                                            .filter(this::isRetryableError)
                                            .doBeforeRetry(
                                                    retrySignal -> log.warn("API 요청 재시도... (Item: {}) | 시도 횟수: {} | 원인: {}",
                                                            logKey, retrySignal.totalRetries() + 1,
                                                            getErrorMessage(retrySignal.failure())))
                                    )
                                    .onErrorResume(error -> {
                                        log.error("API 요청 최종 실패 (Item: {}), 원인: {} | 코드: {}",
                                                logKey, getErrorMessage(error), getErrorCode(error));
                                        return Mono.empty();
                                    });
                        }
                        , maxConcurrentRequests)
                .collectList();
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            return ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();
        }

        if (throwable instanceof ApiException apiException) {
            return !NON_RETRYABLE_ERROR_CODES.contains(apiException.getCode());
        }

        return false;
    }

    private String getErrorCode(Throwable error) {
        if (error instanceof ApiException apiException) {
            return apiException.getCode();
        }
        if (error instanceof WebClientResponseException webException) {
            return "HTTP_" + webException.getStatusCode().value();
        }
        return "UNKNOWN";
    }

    private String getErrorMessage(Throwable error) {
        if (error == null || error.getMessage() == null) {
            return "Unknown error";
        }
        return error.getMessage().replace("\r\n", " ").replace("\n", " ").trim();
    }
}
