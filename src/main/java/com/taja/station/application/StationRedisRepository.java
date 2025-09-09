package com.taja.station.application;

import com.taja.station.presentation.response.NearbyStationResponse;
import com.taja.status.domain.StationStatus;
import com.taja.station.domain.Station;
import java.time.LocalDateTime;
import java.util.List;

public interface StationRedisRepository {
    boolean saveStation(Station station, LocalDateTime requestedAt);

    boolean updateBikeCountAndRequestedAt(StationStatus stationStatus);

    List<NearbyStationResponse> findNearbyStations(double centerLat, double centerLon, double height, double width);
}
