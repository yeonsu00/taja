package com.taja.application.statistics;

import com.taja.domain.statistics.TemperatureStatistics;
import java.util.List;

public interface TemperatureStatisticsRepository {
    void saveTemperatureStatistics(List<TemperatureStatistics> temperatureStatistics);

    List<TemperatureStatistics> findAllByStationIds(List<Long> stationIds);

    List<TemperatureStatistics> findByStationId(Long stationId);
}
