package com.taja.application.statistics.dto;

import java.util.Map;

public record StationHourlyAvg(
        Long stationId,
        Map<Integer, Integer> hourlyAvgParkingBikeCounts
) {
}
