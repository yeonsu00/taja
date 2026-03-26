package com.taja.application.status;

import com.taja.domain.status.StationStatusHourlyAvg;
import java.time.LocalDate;
import java.util.List;

public interface StationStatusHourlyAvgRepository {

    List<StationStatusHourlyAvg> findAllByBaseDate(LocalDate baseDate);

    List<StationStatusHourlyAvg> findAllByBaseDateAndStationNumbers(LocalDate baseDate, List<Integer> stationNumbers);

    List<StationStatusHourlyAvg> findAllByBaseDateAndBaseHourAndStationNumbers(LocalDate baseDate, Integer baseHour,
                                                                               List<Integer> stationNumbers);

    void saveAllHourlyAvgs(List<StationStatusHourlyAvg> stationStatusHourlyAvgs);
}
