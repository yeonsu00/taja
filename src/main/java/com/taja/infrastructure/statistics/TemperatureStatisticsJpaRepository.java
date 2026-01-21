package com.taja.infrastructure.statistics;

import com.taja.domain.statistics.TemperatureStatistics;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemperatureStatisticsJpaRepository extends JpaRepository<TemperatureStatistics, Long> {

    @Query("SELECT t FROM TemperatureStatistics t WHERE t.stationId = :stationId AND t.temperatureRange = :temperatureRange")
    TemperatureStatistics findByStationIdAndTemperatureRange(@Param("stationId") Long stationId, @Param("temperatureRange") Double temperatureRange);

    @Query("SELECT t FROM TemperatureStatistics t WHERE t.stationId IN :stationIds")
    List<TemperatureStatistics> findAllByStationIdIn(@Param("stationIds") List<Long> stationIds);

}







