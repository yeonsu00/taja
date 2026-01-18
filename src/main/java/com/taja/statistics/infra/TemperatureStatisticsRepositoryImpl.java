package com.taja.statistics.infra;

import com.taja.statistics.application.TemperatureStatisticsRepository;
import com.taja.statistics.domain.TemperatureStatistics;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TemperatureStatisticsRepositoryImpl implements TemperatureStatisticsRepository {

    private final TemperatureStatisticsJpaRepository temperatureStatisticsJpaRepository;

    @Override
    public void saveTemperatureStatistics(List<TemperatureStatistics> temperatureStatisticsEntities) {
        temperatureStatisticsJpaRepository.saveAll(temperatureStatisticsEntities);
    }

    @Override
    public List<TemperatureStatistics> findAllByStationIds(List<Long> stationIds) {
        if (stationIds.isEmpty()) {
            return new ArrayList<>();
        }
        return temperatureStatisticsJpaRepository.findAllByStationIdIn(stationIds);
    }
}
