package com.taja.infrastructure.status;

import com.taja.application.status.StationStatusRepository;
import com.taja.domain.status.StationStatus;
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
    public List<StationStatus> findAllByDateAndStationNumbers(LocalDate calculationDate, List<Integer> stationNumbers) {
        if (stationNumbers.isEmpty()) {
            return List.of();
        }
        return stationStatusJpaRepository.findAllByDateAndStationNumbers(calculationDate, stationNumbers);
    }
}
