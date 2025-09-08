package com.taja.status.application;

import com.taja.bikeapi.application.dto.status.StationStatusDto;
import com.taja.station.application.StationRedisRepository;
import com.taja.status.domain.StationStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationStatusService {

    private final StationStatusRepository stationStatusRepository;
    private final StationRedisRepository stationRedisRepository;

    @Transactional
    public void saveStationStatuses(LocalDateTime requestedAt, List<StationStatusDto> loadedStationStatuses) {
        log.info("총 {}개의 대여소 실시간 상태를 성공적으로 수집했습니다.", loadedStationStatuses.size());

        List<StationStatus> stationStatuses = loadedStationStatuses.stream()
                .map(dto -> dto.toStationStatus(requestedAt))
                .toList();

        int savedStationStatusCount = stationStatusRepository.saveAll(stationStatuses);
        log.info("{}개의 대여소 실시간 상태를 DB에 저장했습니다.", savedStationStatusCount);

        updateBikeCountToRedis(stationStatuses);
    }

    private void updateBikeCountToRedis(List<StationStatus> stationStatuses) {
        int redisUpdatedCount = 0;

        for (StationStatus status : stationStatuses) {
            if (stationRedisRepository.updateBikeCountAndRequestedAt(status)) {
                redisUpdatedCount++;
            }
        }

        log.info("Redis 대여소 실시간 상태 총 {}개 업데이트됨.", redisUpdatedCount);
    }

}
