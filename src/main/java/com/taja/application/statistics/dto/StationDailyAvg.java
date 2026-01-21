package com.taja.application.statistics.dto;

public record StationDailyAvg(
        Long stationId,
        Integer dailyAvgParkingBikeCount
) {
}
