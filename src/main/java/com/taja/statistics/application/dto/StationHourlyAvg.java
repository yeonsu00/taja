package com.taja.statistics.application.dto;

import java.util.Map;

public record StationHourlyAvg(
        Long stationId,
        Map<Integer, Integer> hourlyAvgParkingBikeCounts
) {
}
