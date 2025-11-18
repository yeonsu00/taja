package com.taja.statistics.infra;

import com.taja.statistics.application.HourlyStatisticsRepository;
import com.taja.statistics.domain.HourlyStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HourlyStatisticsRepositoryImpl implements HourlyStatisticsRepository {

    private final HourlyStatisticsJpaRepository hourlyStatisticsJpaRepository;

    @Override
    public HourlyStatistics findByStationIdAndHour(Long stationId, int hour) {
        return hourlyStatisticsJpaRepository.findByStationIdAndHour(stationId, hour)
                .map(HourlyStatisticsEntity::toHourlyStatistics)
                .orElse(null);
    }

    @Override
    public void saveHourlyStatistics(HourlyStatistics hourlyStatistics) {
        HourlyStatisticsEntity hourlyStatisticsEntity = HourlyStatisticsEntity.fromHourlyStatistics(hourlyStatistics);
        hourlyStatisticsJpaRepository.save(hourlyStatisticsEntity);
    }

    @Override
    public void updateHourlyStatistics(HourlyStatistics updatedHourlyStatistics) {
        HourlyStatisticsEntity entity = HourlyStatisticsEntity.fromHourlyStatistics(updatedHourlyStatistics);
        hourlyStatisticsJpaRepository.save(entity);
    }
}
