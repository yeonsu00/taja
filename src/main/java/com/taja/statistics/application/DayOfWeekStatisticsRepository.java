package com.taja.statistics.application;

import com.taja.statistics.domain.DayOfWeekStatistics;
import java.time.DayOfWeek;
import java.util.List;

public interface DayOfWeekStatisticsRepository {
    List<DayOfWeekStatistics> findAllByStationIdsAndDayOfWeek(List<Long> stationIds, DayOfWeek dayOfWeek);

    void saveAllDayOfWeekStatistics(List<DayOfWeekStatistics> dayOfWeekStatisticsList);
}
