package com.taja.station.presentation.response;

import java.time.LocalDateTime;
import java.util.List;

public record MapStationResponse(
        Long stationId,
        Integer number,
        double latitude,
        double longitude,
        int bikeCount,
        LocalDateTime requestedAt
) {

    public boolean isAvailable() {
        return bikeCount > 0;
    }

    public static List<Integer> extractAvailableNumbers(List<MapStationResponse> nearbyStations,
                                                        int originStationNumber) {
        return nearbyStations.stream()
                .filter(MapStationResponse::isAvailable)
                .map(MapStationResponse::number)
                .filter(num -> !num.equals(originStationNumber))
                .toList();
    }

}
