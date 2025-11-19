package com.taja.statistics.application;

import com.taja.statistics.domain.TemperatureStatistics;
import java.util.List;

public interface TemperatureStatisticsRepository {
    TemperatureStatistics findByStationIdAndTemperatureRange(Long stationId, Double tempRangeStart);

    void saveAll(List<TemperatureStatistics> temperatureStatisticsEntities);
}
