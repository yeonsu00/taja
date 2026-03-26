package com.taja.infrastructure.statistics;

import com.taja.application.statistics.DayOfWeekStatisticsRepository;
import com.taja.domain.statistics.DayOfWeekStatistics;
import java.time.DayOfWeek;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DayOfWeekStatisticsRepositoryImpl implements DayOfWeekStatisticsRepository {

    private final DayOfWeekStatisticsJpaRepository dayOfWeekStatisticsJpaRepository;

    @Override
    public List<DayOfWeekStatistics> findDayOfWeekStatisticsByStationId(Long stationId) {
        return dayOfWeekStatisticsJpaRepository.findByStationId(stationId);
    }

    @Override
    public List<DayOfWeekStatistics> findAllByStationIdsAndDayOfWeek(List<Long> stationIds, DayOfWeek dayOfWeek) {
        if (stationIds == null || stationIds.isEmpty()) {
            return List.of();
        }
        return dayOfWeekStatisticsJpaRepository.findAllByStationIdsAndDayOfWeek(stationIds, dayOfWeek);
    }

    @Override
    public void saveAllDayOfWeekStatistics(List<DayOfWeekStatistics> dayOfWeekStatisticsList) {
        if (dayOfWeekStatisticsList == null || dayOfWeekStatisticsList.isEmpty()) {
            return;
        }
        dayOfWeekStatisticsJpaRepository.saveAll(dayOfWeekStatisticsList);
    }

}
