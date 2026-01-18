package com.taja.statistics.application;

import com.taja.statistics.domain.TemperatureStatistics;
import java.util.List;

public interface TemperatureStatisticsRepository {
    void saveTemperatureStatistics(List<TemperatureStatistics> temperatureStatistics);

    List<TemperatureStatistics> findAllByStationIds(List<Long> stationIds);
}
