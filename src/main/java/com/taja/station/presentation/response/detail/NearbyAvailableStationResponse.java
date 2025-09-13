package com.taja.station.presentation.response.detail;

public record NearbyAvailableStationResponse(
        Integer number,
        String name,
        Integer distance,
        Integer bikeCount
) {
}
