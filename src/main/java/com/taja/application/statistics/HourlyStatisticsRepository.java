package com.taja.application.statistics;

import com.taja.domain.statistics.HourlyStatistics;
import java.util.List;

public interface HourlyStatisticsRepository {
    List<HourlyStatistics> findAllByStationIds(List<Long> stationIds);

    void saveAllHourlyStatistics(List<HourlyStatistics> hourlyStatisticsList);
}
