package com.taja.collector.application;

import com.taja.collector.infra.api.dto.StationDto;
import com.taja.collector.domain.StationStatus;
import com.taja.collector.infra.api.StationStatusApiClient;
import com.taja.global.exception.StationStatusApiException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationStatusService {

    private final StationStatusApiClient stationStatusApiClient;
    private final StationStatusRepository stationStatusRepository;

    private static final int TOTAL_COUNT = 3000;
    private static final int BATCH_SIZE = 200;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_SECONDS = 1;

    private static final Set<String> NON_RETRYABLE_ERROR_CODES = Set.of(
            "ERROR-300", "ERROR-301", "ERROR-310", "ERROR-331",
            "ERROR-332", "ERROR-333", "ERROR-334", "ERROR-335", "ERROR-336", "INFO-200"
    );

    public void loadStationStatuses() {
        Mono<List<StationDto>> loadedStationStatusesMono = fetchAllStationStatus();

        loadedStationStatusesMono.subscribe(
                loadedStationStatuses -> {
                    log.info("총 {}개의 대여소 정보를 성공적으로 수집했습니다.", loadedStationStatuses.size());

                    LocalDateTime requestedAt = LocalDateTime.now();

                    List<StationStatus> stationStatuses = loadedStationStatuses.stream()
                            .map(stationDto -> stationDto.toStationStatus(requestedAt))
                            .toList();
                    stationStatusRepository.saveAll(stationStatuses);
                },
                error -> {
                    log.error("대여소 정보 수집 중 오류 발생: {}", error.getMessage(), error);
                }
        );


    }

    private Mono<List<StationDto>> fetchAllStationStatus() {
        int pageCount = getPageCount();

        return Flux.range(0, pageCount)
                .flatMap(page -> {
                    int startIndex = 1 + (page * BATCH_SIZE);
                    int endIndex = startIndex + BATCH_SIZE - 1;

                    return stationStatusApiClient.fetchStationStatuses(startIndex, endIndex)
                            .doOnSuccess(stationStatuses -> {
                                log.info("✅ API 요청 성공 ({}-{}) | 수집된 데이터 수: {}",
                                        startIndex, endIndex, stationStatuses.size());
                            })
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

    private static int getPageCount() {
        return (int) Math.ceil((double) TOTAL_COUNT / BATCH_SIZE);
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof StationStatusApiException apiException) {
            return !NON_RETRYABLE_ERROR_CODES.contains(apiException.getCode());
        }
        return true;
    }

    private String getErrorCode(Throwable error) {
        return (error instanceof StationStatusApiException)
                ? ((StationStatusApiException) error).getCode() : "UNKNOWN";
    }

    private String getErrorMessage(Throwable error) {
        return error.getMessage().replace("\r\n", " ").replace("\n", " ").trim();
    }


}
