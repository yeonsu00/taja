package com.taja.status.infra;

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

}
