package com.taja.application.station;

import com.taja.interfaces.api.station.response.MapStationResponse;
import com.taja.domain.status.StationStatus;
import com.taja.domain.station.Station;
import java.time.LocalDateTime;
import java.util.List;

public interface StationRedisRepository {
    void saveStationsWithPipeline(List<Station> stations, LocalDateTime requestedAt);

    void updateBikeCountAndRequestedAtWithPipeline(List<StationStatus> statuses);

    List<MapStationResponse> findNearbyStations(double centerLat, double centerLon, double height, double width);

    List<MapStationResponse> findStationStatus(List<Station> favoriteStations);
}
