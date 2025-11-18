package com.taja.statistics.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class HourlyStatistics {

    private Long hourlyStatisticsId;

    private Long stationId;

    private Integer hour;

    private Integer avgParkingBikeCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public HourlyStatistics(Long hourlyStatisticsId, Long stationId, Integer hour, Integer avgParkingBikeCount,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.hourlyStatisticsId = hourlyStatisticsId;
        this.stationId = stationId;
        this.hour = hour;
        this.avgParkingBikeCount = avgParkingBikeCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static HourlyStatistics create(Long stationId, Integer hour, Integer avgParkingBikeCount) {
        return HourlyStatistics.builder()
                .stationId(stationId)
                .hour(hour)
                .avgParkingBikeCount(avgParkingBikeCount)
                .build();
    }

    public void updateAverage(Integer newAvgParkingBikeCount) {
        this.avgParkingBikeCount = (newAvgParkingBikeCount + this.avgParkingBikeCount) / 2;
    }
}
