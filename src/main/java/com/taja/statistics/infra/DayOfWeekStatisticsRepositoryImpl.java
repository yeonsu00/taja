package com.taja.statistics.infra;

import com.taja.statistics.application.DayOfWeekStatisticsRepository;
import java.time.DayOfWeek;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DayOfWeekStatisticsRepositoryImpl implements DayOfWeekStatisticsRepository {

    private final DayOfWeekStatisticsJpaRepository dayOfWeekStatisticsJpaRepository;


    @Override
    public DayOfWeekStatisticsEntity findByStationIdAndDayOfWeek(Long stationId, DayOfWeek dayOfWeek) {
        return dayOfWeekStatisticsJpaRepository.findByStationIdAndDayOfWeek(stationId, dayOfWeek);
    }

    @Override
    public void save(DayOfWeekStatisticsEntity dayOfWeekStatisticsEntity) {
        dayOfWeekStatisticsJpaRepository.save(dayOfWeekStatisticsEntity);
    }
}
