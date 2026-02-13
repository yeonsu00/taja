package com.taja.application.cache;

import com.taja.interfaces.api.station.response.MapStationResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class StationInfo {

    public record BikeCountInfo(
            Long stationId,
            int availableBikeCount,
            LocalDateTime requestedAt
    ) {
    }

    public record StationGeoInfo(
            Integer number,
            double latitude,
            double longitude
    ) {
    }

    public record NearbyStationGeoInfo(
            Integer number,
            double latitude,
            double longitude,
            double distanceMeters
    ) {
        public static NearbyStationGeoInfo from(Integer number, double latitude, double longitude, double distanceMeters) {
            return new NearbyStationGeoInfo(
                    number,
                    latitude,
                    longitude,
                    distanceMeters
            );
        }
    }

    public record NearbyStationHashInfo(
            Long stationId,
            String name,
            int bikeCount
    ) {
        public static NearbyStationHashInfo from(Long stationId, String name, int bikeCount) {
            return new NearbyStationHashInfo(
                    stationId,
                    name,
                    bikeCount
            );
        }
    }

    public record NearbyAvailableStation(
            Long stationId,
            Integer number,
            String name,
            double latitude,
            double longitude,
            int distanceMeters
    ) {
    }

    public record StationFullInfo(
            Long stationId,
            Integer number,
            double latitude,
            double longitude,
            int bikeCount,
            LocalDateTime requestedAt
    ) {
        public static Optional<StationFullInfo> from(StationHashInfo hashInfo, double lat, double lon) {
            return hashInfo == null ? Optional.empty() : Optional.of(new StationFullInfo(
                    hashInfo.stationId(),
                    hashInfo.number(),
                    lat,
                    lon,
                    hashInfo.bikeCount(),
                    hashInfo.requestedAt()
            ));
        }

        public MapStationResponse toMapStationResponse() {
            return new MapStationResponse(
                    stationId,
                    number,
                    latitude,
                    longitude,
                    bikeCount,
                    requestedAt
            );
        }

        public static List<MapStationResponse> toMapStationResponses(List<StationFullInfo> infos) {
            return infos.stream()
                    .map(StationFullInfo::toMapStationResponse)
                    .toList();
        }
    }

    public record StationHashInfo(
            Integer number,
            Long stationId,
            String name,
            int bikeCount,
            LocalDateTime requestedAt
    ) {
        public static StationHashInfo from(Integer number, Long stationId, String name, int bikeCount, LocalDateTime requestedAt) {
            return new StationHashInfo(
                    number,
                    stationId,
                    name,
                    bikeCount,
                    requestedAt
            );
        }
    }
}
