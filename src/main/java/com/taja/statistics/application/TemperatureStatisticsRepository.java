package com.taja.statistics.application;

import com.taja.statistics.infra.TemperatureStatisticsEntity;
import java.util.List;

public interface TemperatureStatisticsRepository {
    TemperatureStatisticsEntity findByStationIdAndTemperatureRange(Long stationId, Double tempRangeStart);

    void saveAll(List<TemperatureStatisticsEntity> temperatureStatisticsEntities);
}
