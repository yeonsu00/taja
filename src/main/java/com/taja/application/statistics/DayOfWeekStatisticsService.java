package com.taja.application.statistics;

import com.taja.application.statistics.dto.StationDailyAvg;
import com.taja.domain.statistics.DayOfWeekStatistics;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DayOfWeekStatisticsService {

    private final DayOfWeekStatisticsRepository dayOfWeekStatisticsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int processBatch(DayOfWeek dayOfWeek, List<StationDailyAvg> stationDailyAvgParkingBikeCounts) {
        if (stationDailyAvgParkingBikeCounts == null || stationDailyAvgParkingBikeCounts.isEmpty()) {
            log.info("배치 처리할 데이터가 없습니다.");
            return 0;
        }

        List<Long> stationIds = stationDailyAvgParkingBikeCounts.stream()
                .map(StationDailyAvg::stationId)
                .toList();

        List<DayOfWeekStatistics> existingStatistics =
                dayOfWeekStatisticsRepository.findAllByStationIdsAndDayOfWeek(stationIds, dayOfWeek);

        Map<Long, DayOfWeekStatistics> existingDayOfWeekStatisticsMap = new HashMap<>();
        for (DayOfWeekStatistics stats : existingStatistics) {
            existingDayOfWeekStatisticsMap.put(stats.getStationId(), stats);
        }

        List<DayOfWeekStatistics> toSaveDayOfWeekStatistics = new ArrayList<>();
        int updatedCount = 0;

        for (StationDailyAvg stationDailyAvg : stationDailyAvgParkingBikeCounts) {
            Long stationId = stationDailyAvg.stationId();
            Integer avgCount = stationDailyAvg.dailyAvgParkingBikeCount();

            DayOfWeekStatistics existing = existingDayOfWeekStatisticsMap.get(stationId);
            if (existing == null) {
                DayOfWeekStatistics newDayOfWeekStatistics = DayOfWeekStatistics.create(stationId, dayOfWeek, avgCount);
                toSaveDayOfWeekStatistics.add(newDayOfWeekStatistics);
            } else {
                existing.updateAvgParkingBikeCount(avgCount);
                updatedCount++;
            }
        }

        if (!toSaveDayOfWeekStatistics.isEmpty()) {
            dayOfWeekStatisticsRepository.saveAllDayOfWeekStatistics(toSaveDayOfWeekStatistics);
        }

        log.info("요일별 통계 배치 처리 완료 - 신규: {}개, 업데이트: {}개", toSaveDayOfWeekStatistics.size(), updatedCount);
        return toSaveDayOfWeekStatistics.size() + updatedCount;
    }
}

