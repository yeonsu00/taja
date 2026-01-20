package com.taja.statistics.application.dto;

public record StationDailyAvg(
        Long stationId,
        Integer dailyAvgParkingBikeCount
) {
}
