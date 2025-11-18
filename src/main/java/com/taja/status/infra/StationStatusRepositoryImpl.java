package com.taja.status.infra;

import com.taja.status.application.StationStatusRepository;
import com.taja.status.domain.StationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StationStatusRepositoryImpl implements StationStatusRepository {

    private final StationStatusJpaRepository stationStatusJpaRepository;

    @Override
    public int saveAll(List<StationStatus> stationStatuses) {
        List<StationStatusEntity> stationStatusEntities = stationStatuses.stream()
                .map(StationStatusEntity::fromStationStatus)
                .toList();
        return stationStatusJpaRepository.saveAll(stationStatusEntities).size();
    }

    @Override
    public List<StationStatus> findAllByRequestedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<StationStatusEntity> stationStatusEntities = stationStatusJpaRepository.findAllByRequestedAtBetween(
                startDateTime, endDateTime);
        return stationStatusEntities.stream()
                .map(StationStatusEntity::toStationStatus)
                .toList();
    }

    @Override
    public Map<Long, Map<Integer, Integer>> findStationHourlyAverage(LocalDate calculationDate) {
        List<Object[]> results = stationStatusJpaRepository.findStationHourlyAverage(calculationDate);

        Map<Long, Map<Integer, Integer>> stationHourlyAverages = new HashMap<>();
        for (Object[] row : results) {
            Long stationId = ((Number) row[0]).longValue();
            Integer hour = ((Number) row[1]).intValue();
            Integer avgCount = ((Number) row[2]).intValue();

            stationHourlyAverages.computeIfAbsent(stationId, k -> new HashMap<>())
                    .put(hour, avgCount);
        }

        return stationHourlyAverages;
    }
}
