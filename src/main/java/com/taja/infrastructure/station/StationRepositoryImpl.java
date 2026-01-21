package com.taja.infrastructure.station;

import com.taja.global.exception.StationNotFoundException;
import com.taja.application.station.StationRepository;
import com.taja.domain.station.Station;
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
    public List<Station> upsert(List<Station> stations) {
        List<Integer> numbers = stations.stream()
                .map(Station::getNumber)
                .toList();

        Map<Integer, Station> existingStations = stationJpaRepository.findAllByNumberIn(numbers).stream()
                .collect(Collectors.toMap(Station::getNumber, station -> station));

        List<Station> stationsToSave = stations.stream().map(station -> {
            Station existingStation = existingStations.get(station.getNumber());

            if (existingStation == null) {
                return station;
            } else {
                existingStation.update(station);
                return existingStation;
            }
        }).toList();

        return stationJpaRepository.saveAll(stationsToSave);
    }

    @Override
    public List<Station> findByNameContaining(String keyword) {
        return stationJpaRepository.findByNameContaining(keyword);
    }

    @Override
    public Station findStationById(Long stationId) {
        return stationJpaRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException("ID : " + stationId + " 대여소를 찾을 수 없습니다."));
    }

    @Override
    public List<Station> findByNumbers(List<Integer> nearbyStationsNumber) {
        return stationJpaRepository.findAllByNumberIn(nearbyStationsNumber);
    }

    @Override
    public Station findById(Long stationId) {
        return stationJpaRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException(stationId + " 대여소를 찾을 수 없습니다."));
    }

    @Override
    public List<Station> findAll() {
        return stationJpaRepository.findAll();
    }
}
