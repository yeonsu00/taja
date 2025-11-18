package com.taja.status.application;

import com.taja.status.domain.StationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StationStatusRepository {
    int saveAll(List<StationStatus> stationStatuses);

    List<StationStatus> findAllByRequestedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    Map<Long, Map<Integer, Integer>> findStationHourlyAverage(LocalDate calculationDate);
}
