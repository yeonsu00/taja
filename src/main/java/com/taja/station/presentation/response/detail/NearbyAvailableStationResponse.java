package com.taja.station.presentation.response.detail;

public record NearbyAvailableStationResponse(
        Long stationId,
        Integer number,
        String name,
        Integer distance,
        Integer bikeCount
) {
}
