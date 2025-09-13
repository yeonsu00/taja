package com.taja.station.infra;

import com.taja.global.exception.StationNotFoundException;
import com.taja.station.application.StationRepository;
import com.taja.station.domain.Station;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StationRepositoryImpl implements StationRepository {

    private final StationJpaRepository stationJpaRepository;

    @Override
    public int upsert(List<Station> stations) {
        List<Integer> numbers = stations.stream()
                .map(Station::getNumber)
                .toList();

        Map<Integer, StationEntity> existingStations = stationJpaRepository.findAllByNumberIn(numbers).stream()
                .collect(Collectors.toMap(StationEntity::getNumber, entity -> entity));

        List<StationEntity> stationsToSave = stations.stream().map(station -> {
            StationEntity existingStation = existingStations.get(station.getNumber());

            if (existingStation == null) {
                return StationEntity.fromStation(station);
            } else {
                existingStation.update(station);
                return existingStation;
            }
        }).toList();

        return stationJpaRepository.saveAll(stationsToSave).size();
    }

    @Override
    public List<Station> findByNameContaining(String keyword) {
        List<StationEntity> searchedStations = stationJpaRepository.findByNameContaining(keyword);
        return searchedStations.stream()
                .map(StationEntity::toStation)
                .collect(Collectors.toList());
    }

    @Override
    public Station findStationByNumber(int stationNumber) {
        StationEntity stationEntity = stationJpaRepository.findByNumber(stationNumber)
                .orElseThrow(() -> new StationNotFoundException(stationNumber + " 대여소를 찾을 수 없습니다."));
        return stationEntity.toStation();
    }
}
