package com.taja.statistics.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TemperatureStatistics {

    public static final double TEMPERATURE_RANGE_INTERVAL = 5.0;

    private Long temperatureStatisticsId;

    private Long stationId;

    private Double temperatureRange;

    private Integer avgParkingBikeCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public TemperatureStatistics(Long temperatureStatisticsId, Long stationId, Double temperatureRange,
                                Integer avgParkingBikeCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.temperatureStatisticsId = temperatureStatisticsId;
        this.stationId = stationId;
        this.temperatureRange = temperatureRange;
        this.avgParkingBikeCount = avgParkingBikeCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TemperatureStatistics create(Long stationId, Double temperatureRange, Integer avgParkingBikeCount) {
        return TemperatureStatistics.builder()
                .stationId(stationId)
                .temperatureRange(temperatureRange)
                .avgParkingBikeCount(avgParkingBikeCount)
                .build();
    }

    public TemperatureStatistics updateAverage(Integer newAvgParkingBikeCount) {
        return TemperatureStatistics.builder()
                .temperatureStatisticsId(this.temperatureStatisticsId)
                .stationId(this.stationId)
                .temperatureRange(this.temperatureRange)
                .avgParkingBikeCount(newAvgParkingBikeCount)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Double getTemperatureRangeStart(Double temperature) {
        return Math.floor(temperature / TEMPERATURE_RANGE_INTERVAL) * TEMPERATURE_RANGE_INTERVAL;
    }
}
