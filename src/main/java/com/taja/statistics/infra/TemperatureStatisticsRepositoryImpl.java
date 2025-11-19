package com.taja.statistics.infra;

import com.taja.statistics.application.TemperatureStatisticsRepository;
import com.taja.statistics.domain.TemperatureStatistics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TemperatureStatisticsRepositoryImpl implements TemperatureStatisticsRepository {

    private final TemperatureStatisticsJpaRepository temperatureStatisticsJpaRepository;

    @Override
    public TemperatureStatistics findByStationIdAndTemperatureRange(Long stationId, Double tempRangeStart) {
        return temperatureStatisticsJpaRepository.findByStationIdAndTemperatureRange(stationId, tempRangeStart);
    }

    @Override
    public void saveAll(List<TemperatureStatistics> temperatureStatisticsEntities) {
        temperatureStatisticsJpaRepository.saveAll(temperatureStatisticsEntities);
    }
}
