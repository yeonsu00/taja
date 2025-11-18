package com.taja.statistics.application;

import com.taja.statistics.domain.HourlyStatistics;
import com.taja.statistics.infra.HourlyStatisticsEntity;
import com.taja.statistics.infra.HourlyStatisticsJpaRepository;
import com.taja.status.domain.StationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HourlyStatisticsService {

    private final HourlyStatisticsRepository hourlyStatisticsRepository;

    @Transactional
    public void calculateAndSave(Long stationId, List<StationStatus> statuses) {
        log.debug("시간대별 통계 계산 시작 - stationId: {}, 데이터 수: {}", stationId, statuses.size());

        Map<Integer, List<StationStatus>> statusesByHour = statuses.stream()
                .collect(Collectors.groupingBy(status -> status.getRequestedAt().getHour()));

        List<HourlyStatisticsEntity> toSave = new ArrayList<>();
        int updatedCount = 0;

        for (int hour = 0; hour < 24; hour++) {
            List<StationStatus> hourlyStatuses = statusesByHour.get(hour);
            if (hourlyStatuses == null || hourlyStatuses.isEmpty()) {
                continue;
            }

            int avgParkingBikeCount = (int) Math.round(
                    hourlyStatuses.stream()
                            .mapToInt(StationStatus::getParkingBikeCount)
                            .average()
                            .orElse(0.0)
            );

            HourlyStatisticsEntity existing = hourlyStatisticsRepository.findByStationIdAndHour(stationId, hour);

            if (existing != null) {
                HourlyStatistics updated = existing.toDomain().updateAverage(avgParkingBikeCount);
                existing.update(updated);
                updatedCount++;
            } else {
                HourlyStatistics newStats = HourlyStatistics.create(stationId, hour, avgParkingBikeCount);
                toSave.add(HourlyStatisticsEntity.fromDomain(newStats));
            }
        }

        if (!toSave.isEmpty()) {
            hourlyStatisticsRepository.saveAll(toSave);
        }
        
        log.debug("시간대별 통계 저장 완료 - stationId: {}, 신규: {}개, 업데이트: {}개", 
                stationId, toSave.size(), updatedCount);
    }
}

