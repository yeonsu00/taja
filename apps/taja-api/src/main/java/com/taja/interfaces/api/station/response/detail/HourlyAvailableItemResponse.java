package com.taja.interfaces.api.station.response.detail;

public record HourlyAvailableItemResponse(
        int hour,
        int count,
        String baseDate
) {
}
