package com.taja.statistics.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemperatureStatisticsJpaRepository extends JpaRepository<TemperatureStatisticsEntity, Long> {

    @Query("SELECT t FROM TemperatureStatisticsEntity t WHERE t.stationId = :stationId AND t.temperatureRange = :temperatureRange")
    TemperatureStatisticsEntity findByStationIdAndTemperatureRange(@Param("stationId") Long stationId, @Param("temperatureRange") Double temperatureRange);

}





