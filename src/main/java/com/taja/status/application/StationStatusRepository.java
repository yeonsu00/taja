package com.taja.status.application;

import com.taja.status.domain.StationStatus;
import java.time.LocalDate;
import java.util.List;

public interface StationStatusRepository {
    int saveAll(List<StationStatus> stationStatuses);

    List<StationStatus> findByDate(LocalDate calculationDate);

    List<StationStatus> findAllByDateAndStationIds(LocalDate calculationDate, List<Long> stationIds);
}
