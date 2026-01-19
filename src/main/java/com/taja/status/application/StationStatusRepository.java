package com.taja.status.application;

import com.taja.status.domain.StationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StationStatusRepository {
    int saveAll(List<StationStatus> stationStatuses);

//    Map<Long, Map<Integer, Integer>> findStationHourlyAverage(LocalDate calculationDate);
    List<StationStatus> findByDate(LocalDate calculationDate);

    Map<Long, Integer> findStationDailyAverage(LocalDate calculationDate);

    List<StationStatus> findAllByDateAndStationIds(LocalDate calculationDate, List<Long> stationIds);
}
