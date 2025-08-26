package com.taja.station.infra;

import com.taja.station.application.StationRepository;
import com.taja.station.domain.Station;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StationRepositoryImpl implements StationRepository {

    private final StationJpaRepository stationJpaRepository;

    @Override
    public int saveAll(List<Station> stations) {
        List<StationEntity> stationEntities = stations.stream()
                .map(StationEntity::fromStation)
                .toList();
        return stationJpaRepository.saveAll(stationEntities).size();
    }
}
