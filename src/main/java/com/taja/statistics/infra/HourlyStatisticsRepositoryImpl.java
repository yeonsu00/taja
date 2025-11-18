package com.taja.statistics.infra;

import com.taja.statistics.application.HourlyStatisticsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HourlyStatisticsRepositoryImpl implements HourlyStatisticsRepository {

    private final HourlyStatisticsJpaRepository hourlyStatisticsJpaRepository;

    @Override
    public HourlyStatisticsEntity findByStationIdAndHour(Long stationId, int hour) {
        return hourlyStatisticsJpaRepository.findByStationIdAndHour(stationId, hour);
    }

    @Override
    public void saveAll(List<HourlyStatisticsEntity> hourlyStatisticsEntities) {
        hourlyStatisticsJpaRepository.saveAll(hourlyStatisticsEntities);
    }
}
