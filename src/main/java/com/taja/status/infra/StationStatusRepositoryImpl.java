package com.taja.status.infra;

import com.taja.status.application.StationStatusRepository;
import com.taja.status.domain.StationStatus;
import java.time.LocalDate;
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

    @Override
    public Map<Long, Integer> findStationDailyAverage(LocalDate calculationDate) {
        List<Object[]> results = stationStatusJpaRepository.findStationDailyAverage(calculationDate);

        Map<Long, Integer> stationDailyAverages = new HashMap<>();
        for (Object[] row : results) {
            Long stationId = ((Number) row[0]).longValue();
            Integer avgCount = ((Number) row[1]).intValue();

            stationDailyAverages.put(stationId, avgCount);
        }

        return stationDailyAverages;
    }

    @Override
    public List<StationStatus> findAllByDateAndStationIds(LocalDate calculationDate, List<Long> stationIds) {
        if (stationIds.isEmpty()) {
            return List.of();
        }
        List<StationStatusEntity> stationStatusEntities = stationStatusJpaRepository.findAllByDateAndStationIds(
                calculationDate, stationIds);
        return stationStatusEntities.stream()
                .map(StationStatusEntity::toStationStatus)
                .toList();
    }
}
