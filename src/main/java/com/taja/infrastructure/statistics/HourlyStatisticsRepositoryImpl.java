package com.taja.infrastructure.statistics;

import com.taja.application.statistics.HourlyStatisticsRepository;
import com.taja.domain.statistics.HourlyStatistics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HourlyStatisticsRepositoryImpl implements HourlyStatisticsRepository {

    private final HourlyStatisticsJpaRepository hourlyStatisticsJpaRepository;

    @Override
    public List<HourlyStatistics> findAllByStationIds(List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return List.of();
        }
        return hourlyStatisticsJpaRepository.findAllByStationIds(stationIds);
    }

    @Override
    public void saveAllHourlyStatistics(List<HourlyStatistics> hourlyStatisticsList) {
        if (hourlyStatisticsList == null || hourlyStatisticsList.isEmpty()) {
            return;
        }
        hourlyStatisticsJpaRepository.saveAll(hourlyStatisticsList);
    }
}
