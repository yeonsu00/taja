package com.taja.statistics.application;

import com.taja.statistics.infra.DayOfWeekStatisticsEntity;
import java.time.DayOfWeek;

public interface DayOfWeekStatisticsRepository {
    DayOfWeekStatisticsEntity findByStationIdAndDayOfWeek(Long stationId, DayOfWeek dayOfWeek);

    void save(DayOfWeekStatisticsEntity dayOfWeekStatisticsEntity);
}
