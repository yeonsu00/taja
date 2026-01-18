package com.taja.statistics.application.dto;

import java.util.Map;

public record StationHourlyAvgs(Map<Long, Map<Integer, Double>> stationHourAvgParkingCounts) {
}
