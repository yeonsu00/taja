package com.taja.interfaces.api.station.response.detail;

public record DailyAvailableItemResponse(
        String day,
        int count,
        String baseDate
) {
}
