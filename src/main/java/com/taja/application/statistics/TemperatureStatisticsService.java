package com.taja.application.statistics;

import com.taja.domain.statistics.TemperatureStatistics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemperatureStatisticsService {

    private final TemperatureStatisticsRepository temperatureStatisticsRepository;

    public void saveStatistics(List<TemperatureStatistics> temperatureStatistics) {
        temperatureStatisticsRepository.saveTemperatureStatistics(temperatureStatistics);
    }

    public List<TemperatureStatistics> findStatisticsByStationIds(List<Long> stationIds) {
        return temperatureStatisticsRepository.findAllByStationIds(stationIds);
    }

    public List<TemperatureStatistics> findByStationId(Long stationId) {
        return temperatureStatisticsRepository.findByStationId(stationId);
    }
}

