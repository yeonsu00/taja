package com.taja.application.status;

import com.taja.domain.status.StationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StationStatusRepository {
    int saveAllStationStatus(List<StationStatus> stationStatuses);

    List<StationStatus> findByDate(LocalDate calculationDate);

    List<StationStatus> findByRequestedAt(LocalDateTime requestedAt);
}
