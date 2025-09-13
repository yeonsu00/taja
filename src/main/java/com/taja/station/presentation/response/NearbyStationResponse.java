package com.taja.station.presentation.response;

import java.time.LocalDateTime;
import java.util.List;

public record NearbyStationResponse(
        Integer number,
        double latitude,
        double longitude,
        int bikeCount,
        LocalDateTime requestedAt
) {

    public boolean isAvailable() {
        return bikeCount > 0;
    }

    public static List<Integer> extractAvailableNumbers(List<NearbyStationResponse> nearbyStations,
                                                        int originStationNumber) {
        return nearbyStations.stream()
                .filter(NearbyStationResponse::isAvailable)
                .map(NearbyStationResponse::number)
                .filter(num -> !num.equals(originStationNumber))
                .toList();
    }

}
