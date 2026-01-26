package com.taja.application.station;

import com.taja.interfaces.api.station.response.MapStationResponse;
import java.time.LocalDateTime;
import java.util.List;

public class StationInfo {

    public record StationGeoInfo(
            Integer number,
            double latitude,
            double longitude
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
}
