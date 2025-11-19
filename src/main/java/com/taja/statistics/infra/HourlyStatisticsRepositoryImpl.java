package com.taja.statistics.infra;

import com.taja.statistics.application.HourlyStatisticsRepository;
import com.taja.statistics.domain.HourlyStatistics;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HourlyStatisticsRepositoryImpl implements HourlyStatisticsRepository {

    private final HourlyStatisticsJpaRepository hourlyStatisticsJpaRepository;

    @Override
    public Optional<HourlyStatistics> findByStationIdAndHour(Long stationId, int hour) {
        return hourlyStatisticsJpaRepository.findByStationIdAndHour(stationId, hour);
    }

    @Override
    public void save(HourlyStatistics hourlyStatistics) {
        hourlyStatisticsJpaRepository.save(hourlyStatistics);
    }
}
