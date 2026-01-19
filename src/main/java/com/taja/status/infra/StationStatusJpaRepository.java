package com.taja.status.infra;

import com.taja.status.domain.StationStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StationStatusJpaRepository extends JpaRepository<StationStatus, Long> {

//    @Query("""
//        SELECT s.stationId, HOUR(s.requestedTime) AS hour, AVG(s.parkingBikeCount)
//        FROM StationStatus s
//        WHERE s.requestedDate = :date
//        GROUP BY s.stationId, HOUR(s.requestedTime)
//        ORDER BY s.stationId, hour
//    """)
//    List<Object[]> findStationHourlyAverage(@Param("date") LocalDate calculationDate);

    @Query("""
        SELECT s.stationId, AVG(s.parkingBikeCount)
        FROM StationStatus s
        WHERE s.requestedDate = :date
        GROUP BY s.stationId
        ORDER BY s.stationId
    """)
    List<Object[]> findStationDailyAverage(@Param("date") LocalDate calculationDate);

    @Query("""
        SELECT s FROM StationStatus s
        WHERE s.requestedDate = :date
        AND s.stationId IN :stationIds
        ORDER BY s.stationId, s.requestedTime
    """)
    List<StationStatus> findAllByDateAndStationIds(
            @Param("date") LocalDate calculationDate,
            @Param("stationIds") List<Long> stationIds
    );

    List<StationStatus> findByRequestedDate(LocalDate requestedDate);
}
