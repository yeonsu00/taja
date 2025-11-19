package com.taja.statistics.application;

import com.taja.statistics.domain.DayOfWeekStatistics;
import com.taja.status.domain.StationStatus;
import java.time.DayOfWeek;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DayOfWeekStatisticsService {

    private final DayOfWeekStatisticsRepository dayOfWeekStatisticsRepository;

    @Transactional
    public void calculateAndSave(Long stationId, DayOfWeek dayOfWeek, List<StationStatus> statuses) {
        log.debug("요일별 통계 계산 시작 - stationId: {}, dayOfWeek: {}, 데이터 수: {}", 
                stationId, dayOfWeek, statuses.size());

        int avgParkingBikeCount = (int) Math.round(
                statuses.stream()
                        .mapToInt(StationStatus::getParkingBikeCount)
                        .average()
                        .orElse(0.0)
        );

        DayOfWeekStatistics existing = dayOfWeekStatisticsRepository
                .findByStationIdAndDayOfWeek(stationId, dayOfWeek);

        if (existing != null) {
            existing.updateAvgParkingBikeCount(avgParkingBikeCount);
            log.debug("요일별 통계 업데이트 완료 - stationId: {}, dayOfWeek: {}, avg: {}",
                    stationId, dayOfWeek, avgParkingBikeCount);
        } else {
            DayOfWeekStatistics newEntity = DayOfWeekStatistics.create(stationId, dayOfWeek, avgParkingBikeCount);
            dayOfWeekStatisticsRepository.save(newEntity);
            log.debug("요일별 통계 생성 완료 - stationId: {}, dayOfWeek: {}, avg: {}", 
                    stationId, dayOfWeek, avgParkingBikeCount);
        }
    }
}

