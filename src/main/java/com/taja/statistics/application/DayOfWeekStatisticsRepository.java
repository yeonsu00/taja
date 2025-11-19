package com.taja.statistics.application;

import com.taja.statistics.domain.DayOfWeekStatistics;
import java.time.DayOfWeek;

public interface DayOfWeekStatisticsRepository {
    DayOfWeekStatistics findByStationIdAndDayOfWeek(Long stationId, DayOfWeek dayOfWeek);

    void save(DayOfWeekStatistics dayOfWeekStatistics);
}
