package com.taja.station.presentation.response;

import java.time.LocalDateTime;

public record NearbyStationResponse(
        Integer number,
        double latitude,
        double longitude,
        int bikeCount,
        LocalDateTime requestedAt
) {
}
