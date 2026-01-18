package com.taja.statistics.application.dto;

import com.taja.statistics.domain.TemperatureStatistics;

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
