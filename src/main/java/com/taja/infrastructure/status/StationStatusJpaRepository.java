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
        AND s.stationNumber IN :stationNumbers
        ORDER BY s.stationNumber, s.requestedTime
    """)
    List<StationStatus> findAllByDateAndStationNumbers(
            @Param("date") LocalDate calculationDate,
            @Param("stationNumbers") List<Integer> stationNumbers
    );

    List<StationStatus> findByRequestedDate(LocalDate requestedDate);
}
