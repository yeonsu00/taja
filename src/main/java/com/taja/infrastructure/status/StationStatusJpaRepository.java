package com.taja.infrastructure.status;

import com.taja.domain.status.StationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StationStatusJpaRepository extends JpaRepository<StationStatus, Long> {

    List<StationStatus> findByRequestedDate(LocalDate requestedDate);

    @Query("""
        SELECT s FROM StationStatus s
        WHERE s.requestedDate = :requestedDate AND s.requestedTime = :requestedTime
        """)
    List<StationStatus> findByRequestedDateAndRequestedTime(
            @Param("requestedDate") LocalDate requestedDate,
            @Param("requestedTime") LocalTime requestedTime
    );

    java.util.Optional<StationStatus> findTop1ByStationNumberOrderByRequestedDateDescRequestedTimeDesc(
            Integer stationNumber);
}
