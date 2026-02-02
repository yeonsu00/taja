package com.taja.interfaces.api.station.response.detail;

public record TemperatureAvailableItemResponse(
        double temperature,
        int count,
        String baseDate
) {
}
