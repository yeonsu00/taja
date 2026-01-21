package com.taja.application.statistics.dto;

import com.taja.domain.statistics.TemperatureStatistics;

public record StationTempGroup(
        Long stationId,
        Double temperatureRange
) {
    public static StationTempGroup from(TemperatureStatistics stats) {
        return new StationTempGroup(stats.getStationId(), stats.getTemperatureRange());
    }

    public static StationTempGroup from(StationHourlyAnalysis analysis) {
        return new StationTempGroup(analysis.stationId(), analysis.temperature());
    }
}
