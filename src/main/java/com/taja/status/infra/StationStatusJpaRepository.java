package com.taja.status.infra;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StationStatusJpaRepository extends JpaRepository<StationStatusEntity, Long> {

    @Query("SELECT s FROM StationStatusEntity s WHERE s.requestedAt >= :startDateTime AND s.requestedAt < :endDateTime")
    List<StationStatusEntity> findAllByRequestedAtBetween(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("""
        SELECT s.stationId, HOUR(s.requestedAt) AS hour, AVG(s.parkingBikeCount)
        FROM StationStatusEntity s
        WHERE DATE(s.requestedAt) = :date
        GROUP BY s.stationId, HOUR(s.requestedAt)
        ORDER BY s.stationId, hour
    """)
    List<Object[]> findStationHourlyAverage(@Param("date") LocalDate calculationDate);

}
