package com.taja.interfaces.api.station.response.detail;

public record NearbyAvailableStationResponse(
        Integer number,
        String name,
        Integer distance,
        Integer bikeCount
) {
}
