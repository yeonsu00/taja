package com.taja.application.cache;

import com.taja.domain.status.StationStatus;
import com.taja.domain.station.Station;
import java.time.LocalDateTime;
import java.util.List;

public interface StationRedisRepository {
    void saveStations(List<Station> stations, LocalDateTime requestedAt);

    void updateBikeCountAndRequestedAtWithPipeline(List<StationStatus> statuses);

    List<StationInfo.StationGeoInfo> findStationsWithinBox(double centerLat, double centerLon, double height, double width);

    List<StationInfo.NearbyAvailableStation> findNearbyAvailableStations(double centerLat, double centerLon, double radiusKm, Integer excludeNumber);

    List<StationInfo.StationFullInfo> findStationInfos(List<StationInfo.StationGeoInfo> geoInfos);

    List<StationInfo.StationFullInfo> findStationStatus(List<Station> favoriteStations);

    StationInfo.BikeCountInfo getStationStatusByNumber(Integer stationNumber);
}
