package com.taja.application.station;

import com.taja.domain.station.Station;
import java.util.List;

public interface StationRepository {
    List<Station> upsert(List<Station> stations);

    List<Station> findByNameContaining(String keyword);

    Station findStationById(Long stationId);

    List<Station> findByNumbers(List<Integer> nearbyStationsNumber);

    Station findById(Long stationId);

    List<Station> findAll();
}
