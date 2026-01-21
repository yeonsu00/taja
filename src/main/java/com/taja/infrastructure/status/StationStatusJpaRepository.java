package com.taja.infrastructure.status;

import com.taja.domain.status.StationStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StationStatusJpaRepository extends JpaRepository<StationStatus, Long> {

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
