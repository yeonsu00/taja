package com.taja.statistics.application;

import com.taja.statistics.domain.HourlyStatistics;

public interface HourlyStatisticsRepository {
    HourlyStatistics findByStationIdAndHour(Long stationId, int hour);

    void saveHourlyStatistics(HourlyStatistics hourlyStatistics);

    void updateHourlyStatistics(HourlyStatistics existingHourlyStatistics);
}
