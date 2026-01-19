package com.taja.statistics.infra;

import com.taja.statistics.domain.HourlyStatistics;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HourlyStatisticsJpaRepository extends JpaRepository<HourlyStatistics, Long> {

    @Query("SELECT h FROM HourlyStatistics h WHERE h.stationId = :stationId AND h.hour = :hour")
    Optional<HourlyStatistics> findByStationIdAndHour(@Param("stationId") Long stationId, @Param("hour") Integer hour);

    @Query("SELECT h FROM HourlyStatistics h WHERE h.stationId IN :stationIds")
    List<HourlyStatistics> findAllByStationIds(@Param("stationIds") List<Long> stationIds);

}







