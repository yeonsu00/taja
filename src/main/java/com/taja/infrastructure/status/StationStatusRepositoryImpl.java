package com.taja.infrastructure.status;

import com.taja.application.status.StationStatusRepository;
import com.taja.domain.status.StationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StationStatusRepositoryImpl implements StationStatusRepository {

    private final StationStatusJpaRepository stationStatusJpaRepository;

    @Override
    public int saveAllStationStatus(List<StationStatus> stationStatuses) {
        return stationStatusJpaRepository.saveAll(stationStatuses).size();
    }

    @Override
    public List<StationStatus> findByDate(LocalDate calculationDate) {
        return stationStatusJpaRepository.findByRequestedDate(calculationDate);
    }

    @Override
    public List<StationStatus> findByRequestedAt(LocalDateTime requestedAt) {
        return stationStatusJpaRepository.findByRequestedDateAndRequestedTime(
                requestedAt.toLocalDate(),
                requestedAt.toLocalTime()
        );
    }

    @Override
    public Optional<StationStatus> findLatestByStationNumber(Integer stationNumber) {
        return stationStatusJpaRepository.findTop1ByStationNumberOrderByRequestedDateDescRequestedTimeDesc(
                stationNumber);
    }
}
