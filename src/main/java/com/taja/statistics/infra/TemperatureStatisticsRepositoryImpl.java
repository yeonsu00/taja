package com.taja.statistics.infra;

import com.taja.statistics.application.TemperatureStatisticsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TemperatureStatisticsRepositoryImpl implements TemperatureStatisticsRepository {

    private final TemperatureStatisticsJpaRepository temperatureStatisticsJpaRepository;

    @Override
    public TemperatureStatisticsEntity findByStationIdAndTemperatureRange(Long stationId, Double tempRangeStart) {
        return temperatureStatisticsJpaRepository.findByStationIdAndTemperatureRange(stationId, tempRangeStart);
    }

    @Override
    public void saveAll(List<TemperatureStatisticsEntity> temperatureStatisticsEntities) {
        temperatureStatisticsJpaRepository.saveAll(temperatureStatisticsEntities);
    }
}
