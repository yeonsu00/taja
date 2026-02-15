package com.taja.infrastructure.client.bike;

import com.taja.global.exception.ApiException;
import com.taja.global.exception.NoRetryApiException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import org.springframework.web.reactive.function.client.WebClientException;

public enum ErrorLogStrategy {
    NO_RETRY(NoRetryApiException.class, (e, prefix) -> {
        NoRetryApiException ex = (NoRetryApiException) e;
        return String.format("%s 재시도 불가 에러 | CODE: %s, MESSAGE: %s", prefix, ex.getCode(), ex.getMessage());
    }),
    API_FAIL(ApiException.class, (e, prefix) -> {
        ApiException ex = (ApiException) e;
        return String.format("%s 재시도 후 최종 실패 | CODE: %s, MESSAGE: %s", prefix, ex.getCode(), ex.getMessage());
    }),
    TIMEOUT(TimeoutException.class, (e, prefix) ->
            String.format("%s 요청 타임아웃 발생 | 오류: %s", prefix, e.getMessage())),
    WEB_CLIENT(WebClientException.class, (e, prefix) ->
            String.format("%s 네트워크/클라이언트 에러 | 오류: %s", prefix, e.getMessage())),
    UNKNOWN(Throwable.class, (e, prefix) ->
            String.format("%s 알 수 없는 치명적 에러 | 타입: %s, 메시지: %s", prefix, e.getClass().getSimpleName(), e.getMessage()));

    private final Class<? extends Throwable> errorType;
    private final BiFunction<Throwable, String, String> messageFormatter;

    ErrorLogStrategy(Class<? extends Throwable> errorType, BiFunction<Throwable, String, String> messageFormatter) {
        this.errorType = errorType;
        this.messageFormatter = messageFormatter;
    }

    public static String format(Throwable error, String prefix) {
        if (isTimeout(error)) {
            return TIMEOUT.messageFormatter.apply(error, prefix);
        }

        return Arrays.stream(values())
                .filter(strategy -> strategy != TIMEOUT && strategy != UNKNOWN && strategy.errorType.isInstance(error))
                .findFirst()
                .orElse(UNKNOWN)
                .messageFormatter.apply(error, prefix);
    }

    private static boolean isTimeout(Throwable e) {
        return e instanceof TimeoutException
                || (e.getCause() instanceof TimeoutException)
                || (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout"));
    }
}
