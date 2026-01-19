package com.taja.statistics.application;

import com.taja.statistics.domain.HourlyStatistics;
import java.util.List;

public interface HourlyStatisticsRepository {
    List<HourlyStatistics> findAllByStationIds(List<Long> stationIds);

    void saveAllHourlyStatistics(List<HourlyStatistics> hourlyStatisticsList);
}
