package com.taja.status.infra;

import com.taja.status.application.StationStatusRepository;
import com.taja.status.domain.StationStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StationStatusRepositoryImpl implements StationStatusRepository {

    private final StationStatusJpaRepository stationStatusJpaRepository;

    @Override
    public int saveAll(List<StationStatus> stationStatuses) {
        return stationStatusJpaRepository.saveAll(stationStatuses).size();
    }

    @Override
    public List<StationStatus> findByDate(LocalDate calculationDate) {
        return stationStatusJpaRepository.findByRequestedDate(calculationDate);
    }

    @Override
    public List<StationStatus> findAllByDateAndStationIds(LocalDate calculationDate, List<Long> stationIds) {
        if (stationIds.isEmpty()) {
            return List.of();
        }
        return stationStatusJpaRepository.findAllByDateAndStationIds(calculationDate, stationIds);
    }
}
