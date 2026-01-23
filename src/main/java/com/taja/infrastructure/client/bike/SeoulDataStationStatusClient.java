package com.taja.infrastructure.client.bike;

import com.taja.application.status.StatusClient;
import com.taja.global.exception.ApiException;
import com.taja.global.exception.NoRetryApiException;
import com.taja.infrastructure.client.bike.dto.status.BikeApiResponseDto;
import com.taja.infrastructure.client.bike.dto.status.BikeStatusDto;
import com.taja.infrastructure.client.bike.dto.status.StationStatusDto;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Service
public class SeoulDataStationStatusClient implements StatusClient {

    private static final String SUCCESS_RESULT_CODE = "INFO-000";
    private static final Set<String> NON_RETRYABLE_ERROR_CODES = Set.of(
            "ERROR-300", "ERROR-301", "ERROR-310", "ERROR-331",
            "ERROR-332", "ERROR-333", "ERROR-334", "ERROR-335", "ERROR-336", "INFO-200"
    );

    private final WebClient bikeWebClient;
    private final String apiKey;
    private final String apiStatusPath;

    public SeoulDataStationStatusClient(
            @Qualifier("bikeWebClient") WebClient bikeWebClient,
            @Value("${api.bike.key}") String apiKey,
            @Value("${api.bike.status.path}") String apiStatusPath) {
        this.bikeWebClient = bikeWebClient;
        this.apiKey = apiKey;
        this.apiStatusPath = apiStatusPath;
    }

    @Override
    public Mono<List<StationStatusDto>> fetchStationStatuses(int startIndex, int endIndex) {
        String path = getPath(apiStatusPath, startIndex, endIndex);

        return bikeWebClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(BikeApiResponseDto.class)
                .timeout(Duration.ofSeconds(10))
                .map(response -> processResponse(response, startIndex, endIndex))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::shouldRetry)
                        .doBeforeRetry(retrySignal ->
                                log.warn("대여소 상태 API 재시도 ({}-{}) | 시도 횟수: {}",
                                        startIndex, endIndex, retrySignal.totalRetries() + 1)))
                .onErrorResume(error -> handleError(error, startIndex, endIndex));
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof NoRetryApiException) {
            return false;
        }

        if (throwable instanceof ApiException ||
                throwable instanceof TimeoutException ||
                throwable instanceof WebClientException) {
            return true;
        }

        Throwable cause = throwable.getCause();
        return cause instanceof TimeoutException || cause instanceof WebClientException;
    }

    private List<StationStatusDto> processResponse(BikeApiResponseDto response, int startIndex, int endIndex) {
        if (BikeApiResponseDto.hasErrorCode(response)) {
            String resultCode = response.result().code();
            String resultMessage = response.result().message();

            if (NON_RETRYABLE_ERROR_CODES.contains(resultCode)) {
                throw new NoRetryApiException(resultCode, resultMessage);
            }

            throw new ApiException(resultCode, resultMessage);
        }

        if (!BikeApiResponseDto.hasRentBikeStatus(response)) {
            throw new ApiException("JSON_PARSING_ERROR", "API 응답 DTO 파싱에 실패했습니다.");
        }

        BikeStatusDto rentBikeStatus = response.rentBikeStatus();
        String resultCode = rentBikeStatus.result().code();
        String resultMessage = rentBikeStatus.result().message();

        if (SUCCESS_RESULT_CODE.equals(resultCode)) {
            List<StationStatusDto> stationStatuses = rentBikeStatus.stationStatuses();
            log.info("✅ 대여소 상태 API 요청 성공 ({}-{}) | 수집된 데이터 수: {}", startIndex, endIndex, stationStatuses.size());
            return stationStatuses;
        }

        if (NON_RETRYABLE_ERROR_CODES.contains(resultCode)) {
            throw new NoRetryApiException(resultCode, resultMessage);
        }

        throw new ApiException(resultCode, resultMessage);
    }

    private Mono<List<StationStatusDto>> handleError(Throwable error, int startIndex, int endIndex) {
        String prefix = String.format("대여소 상태 API [%d-%d] ", startIndex, endIndex);
        String logMessage = ErrorLogStrategy.format(error, prefix);
        log.error(logMessage);
        return Mono.just(List.of());
    }

    private String getPath(String apiPath, int startIndex, int endIndex) {
        return apiKey + apiPath + startIndex + "/" + endIndex;
    }

}
