package com.taja.application.statistics.dto;

public record StationHourlyAnalysis(
        Long stationId,
        String district,
        Integer hour,
        Integer avgParkingBikeCount,
        Double temperature
) {
}
