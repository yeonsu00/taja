package com.taja.status.infra;

import com.taja.status.application.StationStatusRepository;
import com.taja.status.domain.StationStatus;
import java.util.List;
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
}
