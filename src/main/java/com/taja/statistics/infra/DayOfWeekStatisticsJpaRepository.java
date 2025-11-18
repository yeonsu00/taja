package com.taja.statistics.infra;

import java.time.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DayOfWeekStatisticsJpaRepository extends JpaRepository<DayOfWeekStatisticsEntity, Long> {

    @Query("SELECT d FROM DayOfWeekStatisticsEntity d WHERE d.stationId = :stationId AND d.dayOfWeek = :dayOfWeek")
    DayOfWeekStatisticsEntity findByStationIdAndDayOfWeek(@Param("stationId") Long stationId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

}





