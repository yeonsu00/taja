package com.taja.statistics.application;

import com.taja.statistics.domain.HourlyStatistics;
import java.util.Optional;

public interface HourlyStatisticsRepository {
    Optional<HourlyStatistics> findByStationIdAndHour(Long stationId, int hour);

    void save(HourlyStatistics hourlyStatistics);
}
