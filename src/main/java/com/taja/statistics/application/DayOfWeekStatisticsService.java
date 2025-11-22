package com.taja.statistics.application;

import com.taja.statistics.domain.DayOfWeekStatistics;
import java.time.DayOfWeek;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DayOfWeekStatisticsService {

    private final DayOfWeekStatisticsRepository dayOfWeekStatisticsRepository;

    public void upsertDayOfWeekStatistics(Long stationId, DayOfWeek dayOfWeek, Integer avgCount) {
        DayOfWeekStatistics existingDayOfWeekStatistics = dayOfWeekStatisticsRepository
                .findByStationIdAndDayOfWeek(stationId, dayOfWeek);

        if (existingDayOfWeekStatistics != null) {
            existingDayOfWeekStatistics.updateAvgParkingBikeCount(avgCount);
            log.debug("요일별 통계 업데이트 완료 - stationId: {}, dayOfWeek: {}, avg: {}",
                    stationId, dayOfWeek, avgCount);
        } else {
            DayOfWeekStatistics newDayOfWeekStatistics = DayOfWeekStatistics.create(stationId, dayOfWeek, avgCount);
            dayOfWeekStatisticsRepository.save(newDayOfWeekStatistics);
            log.debug("요일별 통계 생성 완료 - stationId: {}, dayOfWeek: {}, avg: {}",
                    stationId, dayOfWeek, avgCount);
        }
    }

//    public void calculateAndSave(Long stationId, DayOfWeek dayOfWeek, List<StationStatus> statuses) {
//        log.debug("요일별 통계 계산 시작 - stationId: {}, dayOfWeek: {}, 데이터 수: {}",
//                stationId, dayOfWeek, statuses.size());
//
//        int avgParkingBikeCount = (int) Math.round(
//                statuses.stream()
//                        .mapToInt(StationStatus::getParkingBikeCount)
//                        .average()
//                        .orElse(0.0)
//        );
//
//        upsertDayOfWeekStatistics(stationId, dayOfWeek, avgParkingBikeCount);
//    }
}

