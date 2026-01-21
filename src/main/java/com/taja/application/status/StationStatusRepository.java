package com.taja.application.status;

import com.taja.domain.status.StationStatus;
import java.time.LocalDate;
import java.util.List;

public interface StationStatusRepository {
    int saveAll(List<StationStatus> stationStatuses);

    List<StationStatus> findByDate(LocalDate calculationDate);

    List<StationStatus> findAllByDateAndStationIds(LocalDate calculationDate, List<Long> stationIds);
}
