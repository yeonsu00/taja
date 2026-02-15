package com.taja.interfaces.api.station.response.detail;

public record NearbyAvailableStationDetailResponse(
        Long stationId,
        String number,
        String name,
        double latitude,
        double longitude,
        int distance
) {
}
