package com.taja.infrastructure.status;

import com.taja.application.status.StationStatusHourlyAvgRepository;
import com.taja.domain.status.StationStatusHourlyAvg;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StationStatusHourlyAvgRepositoryImpl implements StationStatusHourlyAvgRepository {

    private final StationStatusHourlyAvgJpaRepository stationStatusHourlyAvgJpaRepository;

    @Override
    public List<StationStatusHourlyAvg> findAllByBaseDate(LocalDate baseDate) {
        return stationStatusHourlyAvgJpaRepository.findAllByBaseDate(baseDate);
    }

    @Override
    public List<StationStatusHourlyAvg> findAllByBaseDateAndStationNumbers(LocalDate baseDate,
                                                                           List<Integer> stationNumbers) {
        if (stationNumbers == null || stationNumbers.isEmpty()) {
            return List.of();
        }
        return stationStatusHourlyAvgJpaRepository.findAllByBaseDateAndStationNumbers(baseDate, stationNumbers);
    }

    @Override
    public List<StationStatusHourlyAvg> findAllByBaseDateAndBaseHourAndStationNumbers(LocalDate baseDate,
                                                                                      Integer baseHour,
                                                                                      List<Integer> stationNumbers) {
        if (stationNumbers == null || stationNumbers.isEmpty()) {
            return List.of();
        }
        return stationStatusHourlyAvgJpaRepository.findAllByBaseDateAndBaseHourAndStationNumbers(baseDate, baseHour,
                stationNumbers);
    }

    @Override
    public void saveAllHourlyAvgs(List<StationStatusHourlyAvg> stationStatusHourlyAvgs) {
        if (stationStatusHourlyAvgs == null || stationStatusHourlyAvgs.isEmpty()) {
            return;
        }
        stationStatusHourlyAvgJpaRepository.saveAll(stationStatusHourlyAvgs);
    }
}
