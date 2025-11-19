package com.taja.statistics.infra;

import com.taja.statistics.domain.DayOfWeekStatistics;
import java.time.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DayOfWeekStatisticsJpaRepository extends JpaRepository<DayOfWeekStatistics, Long> {

    @Query("SELECT d FROM DayOfWeekStatistics d WHERE d.stationId = :stationId AND d.dayOfWeek = :dayOfWeek")
    DayOfWeekStatistics findByStationIdAndDayOfWeek(@Param("stationId") Long stationId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

}







