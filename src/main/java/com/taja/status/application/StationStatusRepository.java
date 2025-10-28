package com.taja.status.application;

import com.taja.status.domain.StationStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface StationStatusRepository {
    int saveAll(List<StationStatus> stationStatuses);

    List<StationStatus> findAllByRequestedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
