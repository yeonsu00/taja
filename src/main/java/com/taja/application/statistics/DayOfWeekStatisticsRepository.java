package com.taja.application.statistics;

import com.taja.domain.statistics.DayOfWeekStatistics;
import java.time.DayOfWeek;
import java.util.List;

public interface DayOfWeekStatisticsRepository {
    List<DayOfWeekStatistics> findByStationId(Long stationId);

    List<DayOfWeekStatistics> findAllByStationIdsAndDayOfWeek(List<Long> stationIds, DayOfWeek dayOfWeek);

    void saveAllDayOfWeekStatistics(List<DayOfWeekStatistics> dayOfWeekStatisticsList);
}
