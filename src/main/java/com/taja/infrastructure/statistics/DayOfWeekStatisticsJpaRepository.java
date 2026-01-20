package com.taja.infrastructure.statistics;

import com.taja.domain.statistics.DayOfWeekStatistics;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DayOfWeekStatisticsJpaRepository extends JpaRepository<DayOfWeekStatistics, Long> {

    @Query("SELECT d FROM DayOfWeekStatistics d WHERE d.stationId IN :stationIds AND d.dayOfWeek = :dayOfWeek")
    List<DayOfWeekStatistics> findAllByStationIdsAndDayOfWeek(
            @Param("stationIds") List<Long> stationIds,
            @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

}







