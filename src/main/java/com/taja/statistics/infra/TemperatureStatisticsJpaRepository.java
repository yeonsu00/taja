package com.taja.statistics.infra;

import com.taja.statistics.domain.TemperatureStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemperatureStatisticsJpaRepository extends JpaRepository<TemperatureStatistics, Long> {

    @Query("SELECT t FROM TemperatureStatistics t WHERE t.stationId = :stationId AND t.temperatureRange = :temperatureRange")
    TemperatureStatistics findByStationIdAndTemperatureRange(@Param("stationId") Long stationId, @Param("temperatureRange") Double temperatureRange);

}







