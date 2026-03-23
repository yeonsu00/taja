package com.taja.infrastructure.statistics;

import com.taja.domain.statistics.HourlyStatistics;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HourlyStatisticsJpaRepository extends JpaRepository<HourlyStatistics, Long> {

    @Query("SELECT h FROM HourlyStatistics h WHERE h.stationId IN :stationIds")
    List<HourlyStatistics> findAllByStationIds(@Param("stationIds") List<Long> stationIds);

    List<HourlyStatistics> findByStationId(Long stationId);
}







