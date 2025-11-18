package com.taja.statistics.infra;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HourlyStatisticsJpaRepository extends JpaRepository<HourlyStatisticsEntity, Long> {

    @Query("SELECT h FROM HourlyStatisticsEntity h WHERE h.stationId = :stationId AND h.hour = :hour")
    Optional<HourlyStatisticsEntity> findByStationIdAndHour(@Param("stationId") Long stationId, @Param("hour") Integer hour);

}





