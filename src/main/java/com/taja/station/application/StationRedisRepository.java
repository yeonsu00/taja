package com.taja.station.application;

import com.taja.station.presentation.response.MapStationResponse;
import com.taja.status.domain.StationStatus;
import com.taja.station.domain.Station;
import java.time.LocalDateTime;
import java.util.List;

public interface StationRedisRepository {
    boolean saveStation(Station station, LocalDateTime requestedAt);

    void saveStationsWithPipeline(List<Station> stations, LocalDateTime requestedAt);

    boolean updateBikeCountAndRequestedAt(StationStatus stationStatus);

    void updateBikeCountAndRequestedAtWithPipeline(List<StationStatus> statuses);

    List<MapStationResponse> findNearbyStations(double centerLat, double centerLon, double height, double width);

    List<MapStationResponse> findStationStatus(List<Station> favoriteStations);
}
