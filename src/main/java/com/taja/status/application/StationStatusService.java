package com.taja.status.application;

import com.taja.bikeapi.application.BikeApiBatchFetcher;
import com.taja.bikeapi.application.BikeApiClient;
import com.taja.station.application.StationRedisRepository;
import com.taja.status.domain.StationStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationStatusService {

    private final BikeApiClient bikeApiClient;
    private final BikeApiBatchFetcher bikeApiBatchFetcher;
    private final StationStatusRepository stationStatusRepository;
    private final StationRedisRepository stationRedisRepository;

    private static final int TOTAL_COUNT = 2800;
    private static final int BATCH_SIZE = 200;

    public void loadStationStatuses(LocalDateTime requestedAt) {
        bikeApiBatchFetcher.fetchAll(TOTAL_COUNT, BATCH_SIZE, bikeApiClient::fetchStationStatuses)
                .subscribe(loadedStationStatuses -> {
                    log.info("총 {}개의 대여소 실시간 상태를 성공적으로 수집했습니다.", loadedStationStatuses.size());

                    List<StationStatus> stationStatuses = loadedStationStatuses.stream()
                            .map(dto -> dto.toStationStatus(requestedAt))
                            .toList();

                    int savedStationStatusCount = stationStatusRepository.saveAll(stationStatuses);
                    log.info("{}개의 대여소 실시간 상태를 저장했습니다. ", savedStationStatusCount);

                    updateBikeCountToRedis(stationStatuses);
                });
    }

    private void updateBikeCountToRedis(List<StationStatus> stationStatuses) {
        Flux.fromIterable(stationStatuses)
                .flatMap(stationRedisRepository::updateBikeCountAndRequestedAt)
                .filter(Boolean::booleanValue)
                .collectList()
                .doOnSuccess(list -> log.info("Redis 자전거 수 업데이트 완료! 총 {}개 업데이트됨.", list.size()))
                .subscribe();
    }

}
